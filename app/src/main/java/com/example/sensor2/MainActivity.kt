package com.example.sensor2

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.sensor2.ui.theme.Sensor2Theme
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null
    private var orientationSensor: Sensor? = null
    private val threshold = 100

    private var state by mutableStateOf(SensorState(
        true,
        "",
        "",
        "",
        false
    ))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        setContent {
            Sensor2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.text1)
                        Text(text = state.text2)
                        Text(text = state.text3)
                        SensorButton(
                            symbol = if (state.active)
                                stringResource(R.string.deactivate)
                            else
                                stringResource(R.string.active),
                            modifier = Modifier,
                            onClick = { activeSuspended() }
                        )
                        ColorBox(color = if (state.color) Color.White else Color.Black)
                    }
                }
            }
        }
    }

    private fun activeSuspended() {
        if (state.active) {
            deactivateSensors()
        } else {
            activeSensors()
        }
    }

    override fun onResume() {
        super.onResume()
        activeSensors()
    }

    override fun onPause() {
        super.onPause()
        deactivateSensors()
    }

    private fun activeSensors() {
        state = state.copy(
            active = true,
            text1 = "",
            text2 = ""
        )
        accelerometer?.also { sensor ->
            sensorManager.registerListener(
                this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        lightSensor?.also { sensor ->
            sensorManager.registerListener(
                this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        orientationSensor?.also { sensor ->
            sensorManager.registerListener(
                this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun deactivateSensors() {
        state = state.copy(
            active = false,
            text1 = "",
            text2 = "",
            text3 = ""
        )
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val accelerationX = event.values[0]
                    val accelerationY = event.values[1]
                    val accelerationZ = event.values[2]
                    val accelerationTotal = sqrt(
                        accelerationX * accelerationX +
                                    accelerationY * accelerationY +
                                accelerationZ * accelerationZ
                    )

                    state = state.copy(
                        text1 = "Aceleración:" +
                                "\nX: $accelerationX" +
                                "\nY: $accelerationY" +
                                "\nZ: $accelerationZ" +
                                "\nTotal: $accelerationTotal"
                    )
                }
                Sensor.TYPE_LIGHT -> {
                    val lightValue = event.values[0]
                    state = state.copy(text2 = "Luminosidad: $lightValue")
                    state = if (lightValue >= threshold){
                        state.copy(color= false)
                    } else{
                        state.copy(color= true)
                    }
                }
                Sensor.TYPE_ROTATION_VECTOR -> {
                    val orientation = event.values
                    val orientationX = orientation[0]
                    val orientationY = orientation[1]
                    val orientationZ = orientation[2]
                    state = state.copy(
                        text3 = "$orientationX \n" +
                        "$orientationY \n" +
                        "$orientationZ"
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }
}


data class SensorState(
    val active: Boolean,
    val text1: String,
    val text2: String,
    val text3: String,
    val color: Boolean
)

@Composable
fun SensorButton(
    symbol: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable { onClick() }
            .clip(CircleShape)
            .then(modifier),

        ) {
        Text(
            text = symbol,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 36.sp,
        )

    }
}

@Composable
fun ColorBox(
    color: Color,
){
    Box(
        modifier = Modifier
            .background(color)
            .height(60.dp)
            .width(60.dp)
    )
}





