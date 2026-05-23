const API = "http://localhost:8066/api"

let compareMap
let globe
let renderer
let scene
let camera
let controls
let satellites = []
let globeInitialized = false
let timelineChart


// ===================================
// REAL NDVI SATELLITE LAYER
// ===================================

let ndviSphere
let ndviTexture

const SENTINEL_NDVI_TILE =
"https://services.sentinel-hub.com/ogc/wms/d0eb9df4-4883-4730-8c09-ea8fe4a3c8cc?SERVICE=WMS&REQUEST=GetMap&LAYERS=NDVI&BBOX=-180,-90,180,90&WIDTH=2048&HEIGHT=1024&FORMAT=image/png"



// ===================================
// WAIT FOR DOM
// ===================================

window.addEventListener("DOMContentLoaded", () => {

loadProjects()

setInterval(loadProjects,15000)

})



// ===================================
// LOAD PROJECTS
// ===================================

async function loadProjects(){

try{

const res = await fetch(API + "/projects")
const projects = await res.json()

const table = document.getElementById("projectsTable")

let pending = 0
let verified = 0
let credits = 0

table.innerHTML = `
<tr>
<th>Project</th>
<th>Location</th>
<th>Claimed Trees</th>
<th>Detected Trees (AI)</th>
<th>NDVI</th>
<th>Status</th>
<th>Fraud Risk</th>
<th>Action</th>
</tr>
`

for(const p of projects){
// ===================================
// STATUS STAGE LOGIC
// ===================================

let status = p.status

// Early stage monitoring
if(p.status === "PENDING" && (p.ndvi || 0) < 0.35){
    status = "MONITORING"
}

if(status === "PENDING") pending++
if(status === "VERIFIED") verified++

credits += p.credits || 0

const analysis = vegetationAnalysis(p)

let detected = 0

try{

if(p.latitude && p.longitude){

const ai = await fetch("http://localhost:5001/detect-trees",{
method:"POST",
headers:{
"Content-Type":"application/json"
},
body:JSON.stringify({
latitude:p.latitude,
longitude:p.longitude
})
})

const aiData = await ai.json()

detected = aiData.detectedTrees || 0

p.detectedTrees = detected

}

}catch(e){

console.log("AI detection failed",e)

}

const fraud = calculateFraud(p.treesPlanted, detected)

const fraudColor =
fraud > 70 ? "red" :
fraud > 40 ? "orange" :
"green"

const row = document.createElement("tr")

row.innerHTML = `
<td>${p.name}</td>
<td>${p.location}</td>
<td>${p.treesPlanted}</td>
<td>${detected}</td>
<td>${analysis.ndvi.toFixed(2)}</td>
<td>${status}</td>
<td style="color:${fraudColor};font-weight:bold">${fraud.toFixed(1)}%</td>

<td>
<button onclick="viewProject('${p.id}')">View</button>
<button onclick="showTimeline('${p.id}')">Timeline</button>
<button onclick="verify('${p.id}')">Verify</button>
<button disabled>Reject</button>
<button onclick="listCredits('${p.id}')">Sell Credits</button>
</td>
`

table.appendChild(row)

}

document.getElementById("pendingCount").innerText = pending
document.getElementById("verifiedCount").innerText = verified
document.getElementById("totalCredits").innerText = credits


if(!globeInitialized){

initClimateGlobe(projects)
globeInitialized = true

}else{

updateGlobePoints(projects)

}

}catch(err){

console.error("Project loading failed",err)

}

}



// ===================================
// FRAUD SCORE CALCULATION
// ===================================

function calculateFraud(claimed, detected){

if(!claimed || claimed === 0) return 0

const diff = Math.abs(claimed - detected)

const score = Math.floor((diff / claimed) * 100)

return Math.min(score,100)

}



// ===================================
// VEGETATION ANALYSIS
// ===================================

function vegetationAnalysis(project){

const trees = project.treesPlanted

const ndvi = project.ndvi || Math.min(0.2 + trees/2000,0.85)

let health = "Low"
if(ndvi > 0.5) health = "Healthy"
else if(ndvi > 0.35) health = "Moderate"

const canopy = Math.min(trees/10,80)
const density = Math.min(trees/5,100)

const fraudRisk = detectFraud(project)

const climateScore = Math.floor(
(ndvi*100*0.4) +
(density*0.3) +
((100-fraudRisk)*0.3)
)

return { ndvi, health, canopy, density, fraudRisk, climateScore }

}



// ===================================
// FRAUD DETECTION
// ===================================

function detectFraud(project){

let score = 0

if(project.treesPlanted > 50000) score += 40
if(project.latitude === 0 && project.longitude === 0) score += 40
if(!project.photoUrl) score += 10

score += Math.floor(Math.random()*10)

return score

}



// ===================================
// VIEW PROJECT
// ===================================

async function viewProject(id){

const res = await fetch(API + "/projects")
const projects = await res.json()

const p = projects.find(x => x.id === id)

const analysis = vegetationAnalysis(p)

document.getElementById("projectModal").classList.remove("hidden")

document.getElementById("modalTitle").innerText = p.name
document.getElementById("modalLocation").innerText = "Location: " + p.location
document.getElementById("modalTrees").innerText = "Trees: " + p.treesPlanted

const credits = p.credits || Math.floor(p.treesPlanted / 20)
const co2 = p.co2Offset || p.treesPlanted * 22

document.getElementById("modalCredits").innerText = "Carbon Credits: " + credits
document.getElementById("modalCO2").innerText = "CO₂ Offset: " + co2 + " kg"
document.getElementById("modalNDVI").innerText = "NDVI Score: " + analysis.ndvi.toFixed(2)
// ===================================
// STATUS DISPLAY
// ===================================

let stage = p.status

if(p.status === "PENDING" && analysis.ndvi < 0.35){
    stage = "MONITORING"
}

document.getElementById("modalStatus").innerText = "Verification Stage: " + stage

document.getElementById("modalHealth").innerText = "Vegetation Health: " + analysis.health
document.getElementById("modalDensity").innerText = "Forest Density: " + analysis.density + "%"
document.getElementById("modalCanopy").innerText = "Canopy Coverage: " + analysis.canopy + "%"
document.getElementById("modalFraud").innerText = "Fraud Risk Score: " + analysis.fraudRisk + "%"
document.getElementById("modalImpact").innerText = "Climate Impact Score: " + analysis.climateScore + "/100"

initComparisonMap(p.latitude,p.longitude)
initNDVIChart()

}



// ===================================
// NDVI CHART
// ===================================

function initNDVIChart(){

const ctx = document.getElementById("ndviChart")

new Chart(ctx,{
type:'line',
data:{
labels:["Month 0","Month 3","Month 6","Month 12"],
datasets:[{
label:"NDVI Vegetation Index",
data:[0.18,0.32,0.48,0.62],
borderWidth:3,
borderColor:"#34c759"
}]
}
})

}



// ===================================
// COMPARISON MAP
// ===================================

function initComparisonMap(lat,lon){

if(compareMap){
compareMap.setView([lat,lon],12)
return
}

compareMap = L.map('compareMap').setView([lat,lon],12)

const satelliteLayer = L.tileLayer(
'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
)

const topoLayer = L.tileLayer(
'https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png'
)

satelliteLayer.addTo(compareMap)
topoLayer.addTo(compareMap)

L.control.sideBySide(satelliteLayer,topoLayer).addTo(compareMap)

L.marker([lat,lon]).addTo(compareMap)

}



// ===================================
// VERIFY PROJECT
// ===================================

async function verify(id){

await fetch(API + "/admin/verify/"+id,{method:"POST"})
alert("Project Verified 🌱")
loadProjects()

}



// ===================================
// MARKETPLACE
// ===================================
function listCredits(projectId){

window.currentProject = projectId

document.getElementById("sellProject").innerText =
"Project ID: " + projectId

document.getElementById("sellModal").classList.remove("hidden")

}

function closeSell(){
document.getElementById("sellModal").classList.add("hidden")
}

async function confirmSale(){

const credits = document.getElementById("sellCredits").value
const price = document.getElementById("sellPrice").value

if(!credits || !price){
alert("Please enter credits and price")
return
}

await fetch(API + "/marketplace/list",{
method:"POST",
headers:{
"Content-Type":"application/json"
},
body:JSON.stringify({
projectId: window.currentProject,
credits: parseInt(credits),
price: parseFloat(price),
seller:"Admin"
})
})

alert("Carbon credits listed on marketplace")

closeSell()

loadProjects()

}





// ===================================
// CLIMATE GLOBE
// ===================================

function initClimateGlobe(projects){

if(typeof ThreeGlobe === "undefined"){
console.error("ThreeGlobe not loaded")
return
}

const container = document.getElementById("globeContainer")

const width = container.clientWidth || 1000
const height = 520

scene = new THREE.Scene()
scene.background = new THREE.Color(0x000000)

camera = new THREE.PerspectiveCamera(60,width/height,0.1,1000)
camera.position.z = 280

renderer = new THREE.WebGLRenderer({antialias:true})
renderer.setSize(width,height)

container.innerHTML=""
container.appendChild(renderer.domElement)

controls = new THREE.OrbitControls(camera,renderer.domElement)
controls.autoRotate = true
controls.autoRotateSpeed = 0.4

globe = new ThreeGlobe()

.globeImageUrl("https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg")
.bumpImageUrl("https://unpkg.com/three-globe/example/img/earth-topology.png")

.pointsData(projects)
.pointLat(d=>d.latitude)
.pointLng(d=>d.longitude)
.pointAltitude(0.05)
.pointRadius(0.7)
.pointColor(d => d.status === "VERIFIED" ? "#00ff88" : "#ffcc00")

scene.add(globe)



// ===================================
// REAL NDVI LAYER
// ===================================

loadNDVISatellite()



// FOREST DENSITY LAYER

const forestData = projects.map(p => ({
lat: p.latitude,
lng: p.longitude,
trees: p.detectedTrees || p.treesPlanted || 0
}))

globe
.hexBinPointsData(forestData)
.hexBinPointLat(d => d.lat)
.hexBinPointLng(d => d.lng)
.hexBinPointWeight(d => d.trees)
.hexAltitude(0.02)
.hexTopColor(d => `rgba(0,255,120,${Math.min(1,d.sumWeight/50)})`)
.hexSideColor(() => "rgba(0,0,0,0.05)")



// LIGHTING

const ambient = new THREE.AmbientLight(0xffffff,1.2)
scene.add(ambient)

const dir = new THREE.DirectionalLight(0xffffff,1)
dir.position.set(100,50,100)
scene.add(dir)



createSatellites()
animate()

window.addEventListener("resize",resizeGlobe)

}



// ===================================
// LOAD NDVI SATELLITE
// ===================================

function loadNDVISatellite(){

const loader = new THREE.TextureLoader()

loader.load(

SENTINEL_NDVI_TILE,

function(texture){

ndviTexture = texture

const geometry = new THREE.SphereGeometry(101,64,64)

const material = new THREE.MeshBasicMaterial({
map:texture,
transparent:true,
opacity:0.55
})

ndviSphere = new THREE.Mesh(geometry,material)

scene.add(ndviSphere)

}

)

}



// ===================================
// UPDATE GLOBE
// ===================================

function updateGlobePoints(projects){

if(globe){
globe.pointsData(projects)
}

}



// ===================================
// RESIZE SUPPORT
// ===================================

function resizeGlobe(){

const container = document.getElementById("globeContainer")

if(!renderer) return

const width = container.clientWidth
const height = 520

renderer.setSize(width,height)

camera.aspect = width/height
camera.updateProjectionMatrix()

}



// ===================================
// SATELLITES
// ===================================

function createSatellites(){

const geometry = new THREE.SphereGeometry(2,16,16)
const material = new THREE.MeshBasicMaterial({color:0xffffff})

for(let i=0;i<3;i++){

const sat = new THREE.Mesh(geometry,material)

sat.orbit = 200 + i*20
sat.speed = 0.002 + i*0.001
sat.angle = Math.random()*Math.PI*2

satellites.push(sat)
scene.add(sat)

}

}



// ===================================
// ANIMATION LOOP
// ===================================

function animate(){

requestAnimationFrame(animate)

if(globe) globe.rotation.y += 0.0006

if(ndviSphere) ndviSphere.rotation.y += 0.0006

satellites.forEach(s => {

s.angle += s.speed

s.position.x = Math.cos(s.angle)*s.orbit
s.position.z = Math.sin(s.angle)*s.orbit
s.position.y = 40

})

controls.update()

renderer.render(scene,camera)

}



// ===================================
// CLOSE MODAL
// ===================================

function closeModal(){
document.getElementById("projectModal").classList.add("hidden")
}

// ===================================
// REAL SATELLITE TIMELINE
// ===================================

async function showTimeline(id){

const res = await fetch(API + "/projects")
const projects = await res.json()

const p = projects.find(x => x.id === id)

document.getElementById("timelineModal").classList.remove("hidden")

document.getElementById("timelineTitle").innerText =
"Satellite Timeline: " + p.name

const lat = p.latitude
const lon = p.longitude

// Bounding box around project
const bbox = `${lon-0.01},${lat-0.01},${lon+0.01},${lat+0.01}`

// Project creation date
const startDate = new Date(p.createdAt)

// Calculate timeline dates
const day1 = new Date(startDate)
const day15 = new Date(startDate)
const day30 = new Date(startDate)

day15.setDate(day15.getDate() + 15)
day30.setDate(day30.getDate() + 30)

// Format dates for Sentinel
function format(d){
return d.toISOString().split("T")[0]
}

const date1 = format(day1)
const date15 = format(day15)
const date30 = format(day30)

// Sentinel satellite image URLs

const API_SAT = "http://localhost:8066/api/sentinel/image"

// Day 1 – Satellite evidence
document.getElementById("timelineImg1").src =
`${API_SAT}?lat=${lat}&lon=${lon}&date=${date1}&stage=1`

// Day 15 – NDVI vegetation
document.getElementById("timelineImg2").src =
`${API_SAT}?lat=${lat}&lon=${lon}&date=${date15}&stage=15`

// Day 30 – Forest growth heatmap
document.getElementById("timelineImg3").src =
`${API_SAT}?lat=${lat}&lon=${lon}&date=${date30}&stage=30`
}
// ===================================
// TIMELINE NDVI CHART
// ===================================

function createTimelineChart(project){

const ctx = document.getElementById("timelineChart")

// destroy previous chart if exists
if(timelineChart){
timelineChart.destroy()
}

timelineChart = new Chart(ctx,{
type:'line',
data:{
labels:["Day 1","Day 15","Day 30"],
datasets:[{
label:"NDVI Growth",
data:[
(project.ndvi || 0.2)*0.6,
(project.ndvi || 0.2)*0.8,
(project.ndvi || 0.2)
],
borderColor:"#34c759",
borderWidth:3
}]
}
})

}
function closeTimeline(){
document.getElementById("timelineModal").classList.add("hidden")
}