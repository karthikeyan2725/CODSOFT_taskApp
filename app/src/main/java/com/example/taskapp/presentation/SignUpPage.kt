package com.example.taskapp.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.taskapp.domain.UserViewModel

@Composable
fun SignUpPage(userViewModel: UserViewModel,onClick:()->Unit){
    val userState = userViewModel.userState.collectAsState()
    Column(){
        OutlinedTextField(
            value = userState.value.name ?:"",
            onValueChange = {userViewModel.updateState("name",it)}
        )

        OutlinedTextField(
            value = userState.value.email ?:"",
            onValueChange = {userViewModel.updateState("email",it)}
        )

        OutlinedTextField(
            value = userState.value.password?:"",
            onValueChange ={userViewModel.updateState("password",it)}
        )

        Button(onClick = {userViewModel.signUpUser()}) {
            Text("Sign Up")
        }

        Button(onClick = {onClick()}){
            Text("Login Instead")
        }
    }
}

