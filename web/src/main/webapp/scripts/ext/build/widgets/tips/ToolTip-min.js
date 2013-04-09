/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.ToolTip=Ext.extend(Ext.Tip,{showDelay:500,hideDelay:200,dismissDelay:5000,mouseOffset:[15,18],trackMouse:false,constrainPosition:true,initComponent:function(){Ext.ToolTip.superclass.initComponent.call(this);this.lastActive=new Date();this.initTarget();},initTarget:function(){if(this.target){this.target=Ext.get(this.target);this.target.on('mouseover',this.onTargetOver,this);this.target.on('mouseout',this.onTargetOut,this);this.target.on('mousemove',this.onMouseMove,this);}},onMouseMove:function(e){this.targetXY=e.getXY();if(!this.hidden&&this.trackMouse){this.setPagePosition(this.getTargetXY());}},getTargetXY:function(){return[this.targetXY[0]+this.mouseOffset[0],this.targetXY[1]+this.mouseOffset[1]];},onTargetOver:function(e){if(this.disabled||e.within(this.target.dom,true)){return;}
this.clearTimer('hide');this.targetXY=e.getXY();this.delayShow();},delayShow:function(){if(this.hidden&&!this.showTimer){if(this.lastActive.getElapsed()<this.quickShowInterval){this.show();}else{this.showTimer=this.show.defer(this.showDelay,this);}}else if(!this.hidden&&this.autoHide!==false){this.show();}},onTargetOut:function(e){if(this.disabled||e.within(this.target.dom,true)){return;}
this.clearTimer('show');if(this.autoHide!==false){this.delayHide();}},delayHide:function(){if(!this.hidden&&!this.hideTimer){this.hideTimer=this.hide.defer(this.hideDelay,this);}},hide:function(){this.clearTimer('dismiss');this.lastActive=new Date();Ext.ToolTip.superclass.hide.call(this);},show:function(){this.showAt(this.getTargetXY());},showAt:function(xy){this.lastActive=new Date();this.clearTimers();Ext.ToolTip.superclass.showAt.call(this,xy);if(this.dismissDelay&&this.autoHide!==false){this.dismissTimer=this.hide.defer(this.dismissDelay,this);}},clearTimer:function(name){name=name+'Timer';clearTimeout(this[name]);delete this[name];},clearTimers:function(){this.clearTimer('show');this.clearTimer('dismiss');this.clearTimer('hide');},onShow:function(){Ext.ToolTip.superclass.onShow.call(this);Ext.getDoc().on('mousedown',this.onDocMouseDown,this);},onHide:function(){Ext.ToolTip.superclass.onHide.call(this);Ext.getDoc().un('mousedown',this.onDocMouseDown,this);},onDocMouseDown:function(e){if(this.autoHide!==true&&!e.within(this.el.dom)){this.disable();this.enable.defer(100,this);}},onDisable:function(){this.clearTimers();this.hide();},adjustPosition:function(x,y){var ay=this.targetXY[1],h=this.getSize().height;if(this.constrainPosition&&y<=ay&&(y+h)>=ay){y=ay-h-5;}
return{x:x,y:y};},onDestroy:function(){Ext.ToolTip.superclass.onDestroy.call(this);Ext.getDoc().un('mousedown',this.onDocMouseDown,this);if(this.target){this.target.un('mouseover',this.onTargetOver,this);this.target.un('mouseout',this.onTargetOut,this);this.target.un('mousemove',this.onMouseMove,this);}}});