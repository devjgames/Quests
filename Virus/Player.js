// Player.js
//

var game = org.game.Game.getInstance();
var IO = org.game.IO;
var Log = org.game.Log;
var Collider = org.game.Collider;
var Resource = org.game.Resource;

function create(me) {
	me.properties._collider = new Collider();
	me.properties._collider.radius = 8;
}

function init(me) {
}

function start(me) {
	if(me.scene().isInDesign()) {
		return;
	}
	me.scene().target.set(me.node().position);
	me.scene().target.add(0, 300, 150, me.scene().eye);
	me.scene().up.set(0, 1, 0);
}

function update(me) {
	if(me.scene().isInDesign()) {
		return;
	}
	
	me.properties._collider.velocity.scale(0, 1, 0);
	if(game.buttonDown(0)) {
		var dx = game.mouseX() - game.w() / 2;
		var dy = game.h() / 2 - game.mouseY();
		var d = Math.sqrt(dx * dx + dy * dy);
		
		if(d > 0.0000001) {
			me.scene().move(me.properties._collider.velocity, dx / d * 125, dy / d * 125);
		}
	}
	if(game.buttonDown(1)) {
		me.scene().rotateAroundTarget(-game.dX(), 0);
	}
	me.properties._collider.velocity.y -= 2000 * game.elapsedTime();
	me.properties._collider.resolve(me.scene(), me.scene().root, me.node().position);
	
	var x = me.scene().calcOffset();
	
	me.scene().target.set(me.node().position);
	me.scene().target.y = 0;
	me.scene().target.add(x, me.scene().eye);
}

function renderSprites(me, renderer) {
	var texture = game.getAssets().load(IO.file("sprites.png"));
	
	renderer.beginSprite(texture);
	renderer.push(
		"FPS = " + game.frameRate() + "\n" +
		"RES = " + Resource.getInstances() + "\n" +
		"TRI = " + game.getSceneRenderer().getTrianglesRendered() + "\n" +
		"TST = " + me.properties._collider.getTested(),
		16, 16, 8, 5, 10, 10, 1, 1, 0, 1, 1, 0.5, 0, 1
	);
	renderer.endSprite();
}

function receiveMessage(me, component, type) {
}

function loadSceneName(me) {
    return null;
}

