const API = "http://localhost:8066/api"

console.log("Auth JS loaded")

// =======================
// LOGIN
// =======================

const loginForm = document.getElementById("loginForm")

if (loginForm) {

loginForm.addEventListener("submit", async function(e){

e.preventDefault()

console.log("Login clicked")

const email = document.getElementById("email").value.trim()
const password = document.getElementById("password").value.trim()

try{

const res = await fetch(API + "/auth/login",{
method:"POST",
headers:{
"Content-Type":"application/json"
},
body:JSON.stringify({
email: email,
password: password
})
})

console.log("Status:", res.status)

if(!res.ok){
alert("Invalid email or password ❌")
return
}

const user = await res.json()

localStorage.setItem("userId", user.id)

alert("Login successful 🚀")

window.location.href = "dashboard.html"

}catch(err){

console.log(err)
alert("Server error")

}

})

}


// =======================
// SIGNUP
// =======================

const signupForm = document.getElementById("signupForm")

if (signupForm) {

signupForm.addEventListener("submit", async function(e){

e.preventDefault()

console.log("Signup clicked")

const company = document.getElementById("company").value.trim()
const email = document.getElementById("email").value.trim()
const password = document.getElementById("password").value.trim()

try{

const res = await fetch(API + "/auth/signup",{
method:"POST",
headers:{
"Content-Type":"application/json"
},
body:JSON.stringify({
company: company,
email: email,
password: password
})
})

console.log("Signup status:", res.status)

if(!res.ok){
alert("Signup failed ❌")
return
}

alert("Signup successful 🎉")

window.location.href = "login.html"

}catch(err){

console.log(err)
alert("Server error")

}

})

}