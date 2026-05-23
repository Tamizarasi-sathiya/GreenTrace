const API = "http://localhost:8066/api"

let latitude = null
let longitude = null
let map
// =======================
// MAP
// =======================

const mapElement = document.getElementById("map")

if(mapElement){

map = L.map('map').setView([20.5937,78.9629],4)
// Map Layers
const satellite = L.tileLayer(
'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
{ attribution:'Tiles © Esri' }
)

const street = L.tileLayer(
'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
{ attribution:'© OpenStreetMap' }
)

satellite.addTo(map)

L.control.layers({
"Satellite": satellite,
"Street": street
}).addTo(map)

let marker
let heatLayer

const isSubmitPage = document.getElementById("projectForm")

// =======================
// LIVE CARBON MARKET API CHART
// =======================

document.addEventListener("DOMContentLoaded", function(){

const canvas = document.getElementById("carbonMarketChart")
if(!canvas) return

const ctx = canvas.getContext("2d")

let labels = []
let prices = []

const chart = new Chart(ctx,{
type:"line",
data:{
labels:labels,
datasets:[{
label:"Carbon Market (gCO₂/kWh)",
data:prices,
borderColor:"#2ecc71",
backgroundColor:"rgba(46,204,113,0.2)",
fill:true,
tension:0.4
}]
},
options:{
responsive:true,
maintainAspectRatio:false
}
})

async function updateMarket(){

try{

const res = await fetch("https://api.carbonintensity.org.uk/intensity")
const json = await res.json()

const value =
json.data[0].intensity.actual ??
json.data[0].intensity.forecast

labels.push(new Date().toLocaleTimeString())
prices.push(value)

if(labels.length > 10){
labels.shift()
prices.shift()
}

chart.update()

}catch(err){
console.log("Carbon API error:",err)
}

}

updateMarket()
setInterval(updateMarket,5000)

})

// =======================
// LOCATION SEARCH (TYPE CITY)
// =======================

const locationInput = document.getElementById("location")

if(locationInput){

locationInput.addEventListener("blur", async function(){

const place = locationInput.value
if(!place) return

try{

const res = await fetch(
`https://nominatim.openstreetmap.org/search?format=json&q=${place}`
)

const data = await res.json()

if(data.length > 0){

latitude = parseFloat(data[0].lat)
longitude = parseFloat(data[0].lon)

map.setView([latitude, longitude], 10)

if(marker){
marker.setLatLng([latitude,longitude])
}else{
marker = L.marker([latitude,longitude]).addTo(map)
}

}

}catch(err){
console.log("Location search failed")
}

})

}

// =======================
// LOCATION SELECT (submit page)
// =======================

if(isSubmitPage){

marker = L.marker(map.getCenter()).addTo(map)

map.on("move", function(){

const center = map.getCenter()

latitude = center.lat
longitude = center.lng

marker.setLatLng(center)

})

}

// =======================
// LOAD PROJECT PINS (dashboard only)
// =======================

if(!isSubmitPage){

fetch(API + "/projects")
.then(res => res.json())
.then(projects => {

let heatData = []

projects.forEach(p => {

if(p.latitude && p.longitude){

const credits = Math.floor(p.treesPlanted/20)

const marker = L.marker([p.latitude,p.longitude]).addTo(map)

marker.bindPopup(`
<b>${p.name}</b><br>
📍 ${p.location}<br>
🌳 Trees: ${p.treesPlanted}<br>
💰 Credits: ${credits}<br><br>

<button onclick="buyCreditsFromMap('${p.name}', ${credits}, ${p.latitude}, ${p.longitude})">
Buy Carbon Credits
</button>
`)

heatData.push([
p.latitude,
p.longitude,
p.treesPlanted / 200
])

}

})

heatLayer = L.heatLayer(heatData,{
radius:25,
blur:20,
maxZoom:10
})

if(projects.length > 0){

const group = new L.featureGroup(
projects
.filter(p=>p.latitude && p.longitude)
.map(p=>L.marker([p.latitude,p.longitude]))
)

map.fitBounds(group.getBounds().pad(0.3))

}

const toggleBtn = document.getElementById("toggleHeat")

if(toggleBtn){

toggleBtn.onclick = function(){

if(map.hasLayer(heatLayer)){
map.removeLayer(heatLayer)
}else{
heatLayer.addTo(map)
}

}

}

})

}

}


// =======================
// PROJECT SUBMIT
// =======================

const form = document.getElementById("projectForm")

if(form){

form.addEventListener("submit", async function(e){

e.preventDefault()

if(latitude === null || longitude === null){
alert("Move the map or type location 📍")
return
}

const project = {

name:document.getElementById("name").value,
location:document.getElementById("location").value,
treesPlanted:parseInt(document.getElementById("trees").value),
latitude:latitude,
longitude:longitude

}

try{

await fetch(API + "/projects",{
method:"POST",
headers:{
"Content-Type":"application/json"
},
body:JSON.stringify(project)
})

alert("Project submitted successfully 🌱")

window.location.href="dashboard.html"

}catch(err){

alert("Submission failed")

}

})

}


// =======================
// DASHBOARD PROJECT LOAD
// =======================

async function loadProjects(){

const res = await fetch(API + "/projects")
const projects = await res.json()

let projectCount = 0
let treeCount = 0

let companyProjects = 0
let companyTrees = 0

const container = document.getElementById("projects")

if(!container) return

container.innerHTML = ""

projects.forEach(p => {

projectCount++
treeCount += p.treesPlanted

companyProjects++
companyTrees += p.treesPlanted

const card = document.createElement("div")
card.className = "project-card"

const status = p.status ? p.status.toLowerCase() : "pending"

card.innerHTML = `
<h3>${p.name}</h3>
<p>📍 ${p.location}</p>
<p>🌳 Trees: ${p.treesPlanted}</p>
<span class="status ${status}">
${p.status || "PENDING"}
</span>
`

container.appendChild(card)

})
// =======================
// BUY CREDITS FROM MAP
// =======================

async function buyCreditsFromMap(projectName, credits, lat, lon){

try{

await fetch(API + "/marketplace/list",{
method:"POST",
headers:{
"Content-Type":"application/json"
},
body: JSON.stringify({
projectId: projectName,
credits: 5,
price: 12,
seller: "GreenTrace"
})
})

alert("🌱 Carbon credits purchased!")

drawTradeLine(lat,lon)

}catch(err){

console.log("Purchase failed",err)

}

}


// =======================
// TRADE FLOW LINE
// =======================

function drawTradeLine(lat,lon){

const line = L.polyline(
[
[lat,lon],
[37,-122]
],
{
color:"lime",
weight:3,
dashArray:"6,6"
}
).addTo(map)

setTimeout(()=>{
map.removeLayer(line)
},5000)

}
// GLOBAL STATS

document.getElementById("projectCount").innerText = projectCount
document.getElementById("treeCount").innerText = treeCount

const credits = Math.floor(treeCount/20)
document.getElementById("creditCount").innerText = credits


// =======================
// LIVE CREDIT COUNTER
// =======================

let liveCredits = credits

setInterval(()=>{

liveCredits += Math.floor(Math.random()*2)

const creditEl = document.getElementById("creditCount")

if(creditEl){
creditEl.innerText = liveCredits
}

},5000)


// =======================
// COMPANY IMPACT
// =======================

const companyProjectsEl = document.getElementById("companyProjects")
const companyTreesEl = document.getElementById("companyTrees")
const companyCreditsEl = document.getElementById("companyCredits")

if(companyProjectsEl){

companyProjectsEl.innerText = companyProjects
companyTreesEl.innerText = companyTrees
companyCreditsEl.innerText = Math.floor(companyTrees/20)

}


// =======================
// WALLET
// =======================

const earned = Math.floor(companyTrees/20)
const used = Math.floor(earned * 0.2)
const available = earned - used

const walletEarned = document.getElementById("walletEarned")
const walletUsed = document.getElementById("walletUsed")
const walletAvailable = document.getElementById("walletAvailable")

if(walletEarned){

walletEarned.innerText = earned
walletUsed.innerText = used
walletAvailable.innerText = available

}


// =======================
// IMPACT ANALYTICS CHART
// =======================

const chartCanvas = document.getElementById("projectChart")

if(chartCanvas){

const ctx = chartCanvas.getContext("2d")

new Chart(ctx,{
type:"bar",
data:{
labels:["Projects","Trees","Carbon Credits"],
datasets:[{
label:"Impact",
data:[
projectCount,
treeCount,
Math.floor(treeCount/20)
]
}]
},
options:{
responsive:true,
plugins:{legend:{display:false}}
}
})

}

}
// =======================
// LOAD MARKETPLACE
// =======================

async function loadMarketplace(){

const res = await fetch(API + "/marketplace")
const listings = await res.json()

const container = document.getElementById("marketplaceListings")

if(!container) return

container.innerHTML = ""

listings.forEach(l => {

const card = document.createElement("div")
card.className = "project-card"

card.innerHTML = `
<h3>Carbon Credits</h3>
<p>Project: ${l.projectId}</p>
<p>Credits: ${l.credits}</p>
<p>Price: $${l.price}</p>

<button onclick="buyCredits('${l.id}')">
Buy Credits
</button>
`

container.appendChild(card)

})

}
// =======================
// BUY CREDITS
// =======================

async function buyCredits(id){

await fetch(API + "/marketplace/buy/" + id,{
method:"POST"
})

alert("Credits purchased 🌱")

loadMarketplace()

}
// =======================
// DOWNLOAD CERTIFICATE
// =======================

function downloadCertificate(){

const credits =
document.getElementById("walletAvailable").innerText

window.open(
API + "/certificate/generate?company=GreenTraceUser&credits=" + credits
)

}
// =======================
// RUN DASHBOARD
// =======================

if(document.getElementById("projects")){
loadProjects()
loadMarketplace()
}
