for(var i = 0; i != 30; i++) {
	var a = i / 28.0 * Math.PI * 2;
	var r = ((i % 2) == 0) ? 0.5 : 0.6;
	var x = Math.cos(a) * r;
	var y = Math.sin(a) * r;
	
	lines.addPoint(x, y);
}

for(var i = 0; i != 30; i++) {
	lines.addLine(i, (i + 1) % 29);
}
