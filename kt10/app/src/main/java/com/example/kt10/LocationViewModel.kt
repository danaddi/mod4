package com.example.kt10

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

sealed class LocationUiState {
    object Idle : LocationUiState()
    object Loading : LocationUiState()
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val address: AddressInfo
    ) : LocationUiState()
    data class Error(val message: String) : LocationUiState()
}

data class AddressInfo(
    val fullAddress: String,
    val street: String? = null,
    val city: String? = null,
    val country: String? = null,
    val postalCode: String? = null
)

class LocationViewModel(
    private val context: Context
) : ViewModel() {

    var uiState by mutableStateOf<LocationUiState>(LocationUiState.Idle)
        private set

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val cancellationTokenSource = CancellationTokenSource()

    fun getCurrentLocation() {
        uiState = LocationUiState.Loading

        viewModelScope.launch {
            try {
                val location = getLastKnownLocation()

                if (location != null) {
                    processLocation(location)
                } else {
                    val currentLocation = getCurrentLocationFromProvider()
                    if (currentLocation != null) {
                        processLocation(currentLocation)
                    } else {
                        uiState = LocationUiState.Error(
                            "Не удалось получить местоположение. Проверьте GPS и попробуйте снова."
                        )
                    }
                }
            } catch (e: SecurityException) {
                uiState = LocationUiState.Error(
                    "Нет разрешения на определение местоположения"
                )
                Log.e("LocationViewModel", "Security exception", e)
            } catch (e: Exception) {
                uiState = LocationUiState.Error(
                    "Ошибка: ${e.message ?: "Неизвестная ошибка"}"
                )
                Log.e("LocationViewModel", "Error getting location", e)
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getLastKnownLocation(): Location? {
        return try {
            val locationResult = fusedLocationClient.lastLocation.await()
            locationResult
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error getting last location", e)
            null
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocationFromProvider(): Location? {
        return try {
            val locationResult = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
            locationResult
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error getting current location", e)
            null
        }
    }

    private suspend fun processLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        val addressInfo = getAddressFromLocation(latitude, longitude)

        uiState = LocationUiState.Success(
            latitude = latitude,
            longitude = longitude,
            address = addressInfo
        )
    }

    private suspend fun getAddressFromLocation(
        latitude: Double,
        longitude: Double
    ): AddressInfo {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val fullAddress = address.getAddressLine(0) ?: buildAddress(address)
                    val street = address.thoroughfare ?: address.featureName
                    val city = address.locality ?: address.subAdminArea
                    val country = address.countryName
                    val postalCode = address.postalCode

                    AddressInfo(
                        fullAddress = fullAddress,
                        street = street,
                        city = city,
                        country = country,
                        postalCode = postalCode
                    )
                } else {
                    AddressInfo(
                        fullAddress = "Адрес не найден"
                    )
                }
            } catch (e: IOException) {
                Log.e("LocationViewModel", "Geocoder error", e)
                AddressInfo(
                    fullAddress = "Ошибка получения адреса. Проверьте интернет-соединение."
                )
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Unexpected error", e)
                AddressInfo(
                    fullAddress = "Ошибка: ${e.message}"
                )
            }
        }
    }

    private fun buildAddress(address: Address): String {
        val parts = mutableListOf<String>()

        address.getAddressLine(0)?.let { parts.add(it) }
        address.locality?.let { parts.add(it) }
        address.subAdminArea?.let { parts.add(it) }
        address.countryName?.let { parts.add(it) }

        return if (parts.isEmpty()) "Адрес не найден" else parts.joinToString(", ")
    }

    override fun onCleared() {
        super.onCleared()
        cancellationTokenSource.cancel()
    }
}