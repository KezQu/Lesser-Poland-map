const url = "http://localhost:2137";

const cnv = document.getElementById("draw_map");
const ctx = cnv.getContext("2d");

let mouseCoords;
const contextScale = 5;
let voivodeshipArea;
let markedPoints = [];
let distancePoints = {p1 : undefined, p2 : undefined};

function getSessionCookieValue(){
	let cookies = document.cookie;
	let csrfCookie = cookies.match("XSRF-TOKEN=[a-zA-Z0-9\-]*")[0];
	let token = {name : "", value : ""};
	token.name = csrfCookie.split('=')[0];
	token.value = csrfCookie.split('=')[1];
	return token.value;
}

function resizeCanvasToFit(){
	document.getElementById("draw_map").style.width = document.getElementById("map_context").clientWidth + "px";
	document.getElementById("draw_map").style.height = document.getElementById("map_context").clientHeight + "px";
}

function loadVoivodeship(){
	const resPromise = fetch(url + "/voivodeshipborder/");
	resizeCanvasToFit()
	resizeCanvas();
	resPromise.then(res => res.json()).then(res => {
		voivodeshipArea = res;
		drawArea(voivodeshipArea.borderPolygon, 2, "#31081F");
	});
}
function drawArea(border, lineWidth, color){
	ctx.beginPath();
	for(let i = 0; i < border.points.length - 1; i++){
		ctx.moveTo(contextScale * (border.points[i].x), contextScale * (border.points[i].y));
		ctx.lineTo(contextScale * (border.points[i+1].x), contextScale * (border.points[i+1].y));
		ctx.lineWidth = lineWidth;
		ctx.strokeStyle = color;
		ctx.stroke();
	}
	ctx.closePath();
}
function resetCanvas(){
	ctx.reset();
	ctx.translate(0, ctx.canvas.height);
	ctx.scale(1,-1);
	drawArea(voivodeshipArea.borderPolygon, 2, "#31081F");
}
function resizeCanvas(){
	fetch(url + "/voivodeshipborder/boundingbox")
		.then(res => res.json())
		.then(res =>{
			ctx.canvas.width = (res[0].x + res[1].x) * contextScale;
			ctx.canvas.height = (res[0].y + res[1].y) * contextScale;
			ctx.translate(0, ctx.canvas.height);
			ctx.scale(1,-1);
		});
}

function checkMouseCoords(){
	let offset = document.getElementById("map_context").getBoundingClientRect();
	mouseCoords = {x : 0 , y : 0};
	if (offset.left > window.event.clientX){
		mouseCoords.x = 0;
	}
	else if (offset.right < window.event.clientX){
		mouseCoords.x = (offset.right - offset.left) / document.getElementById("draw_map").clientWidth;
	}
	else{
		mouseCoords.x = Math.floor(window.event.clientX - offset.left) / document.getElementById("draw_map").clientWidth;
	}

	if (offset.top > window.event.clientY){
		mouseCoords.y = (offset.bottom - offset.top) / document.getElementById("draw_map").clientHeight;
	}
	else if (offset.bottom < window.event.clientY){
		mouseCoords.y = 0;
	}
	else{
		mouseCoords.y = -Math.floor(window.event.clientY - offset.bottom) / document.getElementById("draw_map").clientHeight;
	}
	mouseCoords.x = (mouseCoords.x / contextScale) * ctx.canvas.width;
	mouseCoords.y = (mouseCoords.y / contextScale) * ctx.canvas.height;
}

function drawClickedPoint(mouseCoords){
	ctx.beginPath();
	ctx.arc(contextScale * mouseCoords.x, contextScale * mouseCoords.y, 2, 0, 2 * Math.PI);
	ctx.fillStyle = "#31081F";
	ctx.strokeStyle = "#31081F";
	ctx.fill();
	ctx.stroke();
	ctx.closePath();
}
function drawLine(prevPoint, currPoint){
	ctx.beginPath();
	ctx.moveTo(contextScale * (prevPoint.x), contextScale * (prevPoint.y));
	ctx.lineTo(contextScale * (currPoint.x), contextScale * (currPoint.y));
	ctx.fillStyle = "#31081F";
	ctx.strokeStyle = "#31081F";
	ctx.stroke();
	ctx.closePath();
}

async function processMouseClick(event){
	let details = "";
	if(event.ctrlKey){
		details = await calculateMarkedArea();
	}
	else if(event.shiftKey){
		details = await calculateDistance();
	}
	else{
		details = await checkIsInside();
	}
	document.getElementById("details_info").innerHTML = details;
}
async function calculateDistance(){
	let details = "";
	if(distancePoints.p1 != undefined && distancePoints.p2 != undefined){
	}
	if(distancePoints.p1 == undefined){
		resetCanvas();
		distancePoints.p1 = mouseCoords;
		drawClickedPoint(distancePoints.p1);
	}
	else if(distancePoints.p2 == undefined){
		distancePoints.p2 = mouseCoords;
		drawClickedPoint(distancePoints.p2);
		drawLine(distancePoints.p1, distancePoints.p2);
		let req = {
			method : "POST",
			headers: {
				"Content-Type": "application/json",
				"Accept": "application/json",
				"X-XSRF-TOKEN" : getSessionCookieValue()
			  },
			  body: JSON.stringify(distancePoints)
		};
		await fetch(url + "/operation/distance",req)
			.then(res => res.json())
			.then(res => {
				details += "<tr><td><p class=\"text\"> calculated distance: " + res.distance + " km</p></td></tr>";
			});
		distancePoints = {p1 : undefined, p2 : undefined};
	}
	return details;
}
async function calculateMarkedArea(){
	let details = "";
	if(markedPoints.length ==0){
		resetCanvas();
	}
	markedPoints.push(mouseCoords);
	drawClickedPoint(mouseCoords);
	if(markedPoints.length >= 2){
		drawLine(markedPoints[markedPoints.length - 2], markedPoints[markedPoints.length - 1]);
		if(Math.sqrt(Math.pow(markedPoints[0].x - markedPoints[markedPoints.length - 1].x, 2) + 
		Math.pow(markedPoints[0].y - markedPoints[markedPoints.length - 1].y, 2)) < 1){
			markedPoints[markedPoints.length - 1] = markedPoints[0];
			let req = {
				method : "POST",
				headers: {
					"Content-Type": "application/json",
					"Accept": "application/json",
					"X-XSRF-TOKEN" : getSessionCookieValue()
				  },
				  body: JSON.stringify(markedPoints)
			};
			await fetch(url + "/operation/area",req)
				.then(res => res.json())
				.then(res => {
					details += "<tr><td><p class=\"text\"> calculated area: " + res.area + " km^2</p></td></tr>";
				});
			markedPoints = [];
		}
	}
	return details;
}
async function checkIsInside(){
	let req = {
		method : "POST",
		headers: {
			"Content-Type": "application/json",
			"Accept": "application/json",
			"X-XSRF-TOKEN" : getSessionCookieValue()
		  },
		  body: JSON.stringify(mouseCoords)
	};
	const resPromise = fetch(url + "/voivodeshipborder/isinside", req);
	resetCanvas();
	drawClickedPoint(JSON.parse(req.body));
	let details = "";
	await resPromise.then(res => res.json())
		.then(res =>{
			let colors = ["#6B0F1A","#808F85", "#31081F","#DCE0D9"]
			for(let i = 0; i < res.length; i++){
				drawArea(res[i].borderPolygon, 1, colors[i % 4]);
				details += "<tr><td style=\"border-color: " + colors[i % 4] + ";\"><p class=\"text\">" + res[i].areaName;
				if(res[i].areaNamePrefix != undefined){
					details +=  ", " + res[i].areaNamePrefix;
				}
				details += "</p>\n";
				if(res[i].population == 0){
					res[i].population = "not specified";
				}
				details +="<p class=\"text\">population: " + res[i].population + "</p></td>\n"
				details += "</tr>\n";
			}
		});
		return details;
} 