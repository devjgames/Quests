// Door.js
//

var game = org.game.Game.getInstance();
var IO = org.game.IO;
var Log = org.game.Log;

function create(me) {
	me.properties._y = 0;
}

function init(me) {
	me.node().setMeshPartDecal(0, game.getAssets().load(IO.file("door-glow.png")));
	me.properties._y = me.node().position.y;
}

function start(me) {
}

function update(me) {
	if(me.scene().isInDesign()) {
		return;
	}
	var px = me.node().absolutePosition.x;
	var pz = me.node().absolutePosition.z;
	var ex = me.scene().eye.x;
	var ez = me.scene().eye.z;
	var dx = px - ex;
	var dz = pz - ez;
	var d = Math.sqrt(dx * dx + dz * dz);
	var a = 1 - Math.min(d / 64, 1);
	
	me.node().position.y = me.properties._y - a * 150;
}

function renderSprites(me, renderer) {
}

function receiveMessage(me, component, type) {
}

function loadSceneName(me) {
    return null;
}

