package com.example.myapplication.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.models.LoginRequest
import com.example.myapplication.network.RetrofitInstance
import com.example.myapplication.models.User
import com.example.myapplication.session.SessionManager

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    navController: NavController
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Login")

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {


                android.util.Log.d(
                    "LOGIN_DEBUG",
                    "Button clicked"
                )

                RetrofitInstance.api.login(
                    LoginRequest(
                        email,
                        password
                    )
                ).enqueue(
                    object : Callback<User> {

                        override fun onResponse(
                            call: Call<User>,
                            response: Response<User>
                        ) {

                            android.util.Log.d(
                                "LOGIN_DEBUG",
                                "Response code = ${response.code()}"
                            )

                            if(response.isSuccessful) {

                                val user = response.body()

                                android.util.Log.d(
                                    "LOGIN_DEBUG",
                                    "Login success user=${user?.id}"
                                )

                                SessionManager.currentUserId =
                                    user?.id

                                navController.navigate("profile")
                            }
                        }

                        override fun onFailure(
                            call: Call<User>,
                            t: Throwable
                        ) {

                            android.util.Log.e(
                                "LOGIN_DEBUG",
                                "Login failed",
                                t
                            )
                        }
                    }
                )

            }
        ) {
            Text("Login")
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "Don't have an account? Register",
            modifier = Modifier.clickable {
                navController.navigate("register")
            }
        )
    }
}