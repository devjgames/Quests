for(var i = 0; i != 12; i++) {
	var a = i / 10.0 * Math.PI * 2;
	var r = ((i % 2) == 0) ? 0.25 : 0.5;
	var x = Math.cos(a) * r;
	var y = Math.sin(a) * r;
	
	lines.addPoint(x, y);
}

for(var i = 0; i != 12; i++) {
	lines.addLine(i, (i + 1) % 11);
}
