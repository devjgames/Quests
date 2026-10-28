// Fire.js
//

var game = org.game.Game.getInstance();
var IO = org.game.IO;
var Log = org.game.Log;
var ParticleSystem = org.game.ParticleSystem;
var Particle = org.game.Particle;
var Random = java.util.Random;

function create(me) {
	me.properties._particle = new Particle();
	me.properties._random = new Random(100);
}

function init(me) {
	me.node().renderable = new ParticleSystem(500);
	me.node().renderable.texture = game.getAssets().load(IO.file("fire.png"));
}

function start(me) {
}

function update(me) {
	var p = me.properties._particle;
	var r = me.properties._random;
	var sc = 0.5 + r.nextFloat() * 0.5;
	var ss = 10 + r.nextFloat() * 20;
	var ls = 0.5 + r.nextFloat() * 1.5;
	
	p.velocityX = 0;
	p.velocityY = 10 + r.nextFloat() * 10;
	p.velocityZ = 0;
	p.positionX = -2 + r.nextFloat() * 4;
	p.positionY = -2 + r.nextFloat() * 4;
	p.positionZ = -2 + r.nextFloat() * 4;
	p.startR = p.startG = p.startB = sc;
	p.startA = 1;
	p.endR = p.endG = p.endB = 0;
	p.endA = 1;
	p.startX = p.startY = ss;
	p.endX = p.endY = 1;
	p.lifeSpan = ls;
	
	me.node().renderable.emit(p);
}

function renderSprites(me, renderer) {
}

function receiveMessage(me, component, type) {
}

function loadSceneName(me) {
    return null;
}

