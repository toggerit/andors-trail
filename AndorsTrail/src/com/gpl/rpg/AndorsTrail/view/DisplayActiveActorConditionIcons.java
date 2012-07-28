package com.gpl.rpg.AndorsTrail.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.actor.Actor;
import com.gpl.rpg.AndorsTrail.model.listeners.ActorConditionListener;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;

public class DisplayActiveActorConditionIcons implements ActorConditionListener {
	
	private final TileManager tileManager;
	private final RelativeLayout activeConditions;
	private final ArrayList<ActiveConditionIcon> currentConditionIcons = new ArrayList<ActiveConditionIcon>();
	private final WeakReference<Context> androidContext;
	
	public DisplayActiveActorConditionIcons(final TileManager tileManager, Context androidContext, RelativeLayout activeConditions) {
		this.tileManager = tileManager;
		this.androidContext = new WeakReference<Context>(androidContext);
		this.activeConditions = activeConditions;
	}
	
	private ActiveConditionIcon getIconFor(ActorCondition condition) {
		for (ActiveConditionIcon icon : currentConditionIcons) {
			if (icon.condition == condition) return icon;
		}
		return null;
	}
	private ActiveConditionIcon getFirstFreeIcon() {
		for (ActiveConditionIcon icon : currentConditionIcons) {
			if (!icon.isVisible()) return icon;
		}
		return addNewActiveConditionIcon();
	}
	
	private RelativeLayout.LayoutParams getLayoutParamsForIconIndex(int index) {
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		if (index == 0) {
			layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		} else {
			layout.addRule(RelativeLayout.LEFT_OF, currentConditionIcons.get(index-1).id);
		}
		return layout;
	}

	private ActiveConditionIcon addNewActiveConditionIcon() {
		int index = currentConditionIcons.size();
				
		ActiveConditionIcon icon = new ActiveConditionIcon(androidContext.get(), index+1);
		
		activeConditions.addView(icon.image, getLayoutParamsForIconIndex(index));
		
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.ALIGN_RIGHT, icon.id);
		layout.addRule(RelativeLayout.ALIGN_BOTTOM, icon.id);
		activeConditions.addView(icon.text, layout);
		
		/*
		layout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.ALIGN_RIGHT, icon.image.getId());
		layout.addRule(RelativeLayout.ALIGN_BOTTOM, icon.image.getId());
		activeConditions.addView(icon.duration, layout);
		*/
		
		currentConditionIcons.add(icon);
		
		return icon;
	}

	private final class ActiveConditionIcon implements AnimationListener {
		public final int id;
		public ActorCondition condition;
		public final ImageView image;
		public final TextView text;
		//public final TextView duration;
		private final Animation onNewIconAnimation;
		private final Animation onRemovedIconAnimation;
		private final Animation onAppliedEffectAnimation;
		
		public ActiveConditionIcon(Context context, int id) {
			this.id = id;
			this.image = new ImageView(context);
			this.image.setId(id);
			this.text = new TextView(context);
			this.onNewIconAnimation = AnimationUtils.loadAnimation(context, R.anim.scaleup);
			this.onRemovedIconAnimation = AnimationUtils.loadAnimation(context, R.anim.scaledown);
			this.onAppliedEffectAnimation = AnimationUtils.loadAnimation(context, R.anim.scalebeat);
			this.onRemovedIconAnimation.setAnimationListener(this);
			//duration = new TextView(context);
			
			final Resources res = context.getResources();
			
			//float textSize = ;
			//magnitude.setTextSize(res.getDimension(R.dimen.smalltext));
			/*
			duration.setTextSize(res.getDimension(R.dimen.smalltext));
			
			int textColor = res.getColor(android.R.color.white);
			int shadowColor = res.getColor(android.R.color.black);
			magnitude.setTextColor(textColor);
			duration.setTextColor(textColor);
			
			magnitude.setShadowLayer(1, 1, 1, shadowColor);
			duration.setShadowLayer(2, 1, 1, shadowColor);
			*/
			
			text.setTextColor(res.getColor(android.R.color.white));
			text.setShadowLayer(1, 1, 1, res.getColor(android.R.color.black));
		}
		
		private void setActiveCondition(ActorCondition condition) {
			this.condition = condition;
			tileManager.setImageViewTile(image, condition.conditionType);
			image.setVisibility(View.VISIBLE);
			setIconText();
		}

		public void setIconText() {
			boolean showMagnitude = (condition.magnitude != 1);
			boolean showDuration = condition.isTemporaryEffect();
			if (showMagnitude/* || showDuration*/) {
				/*if (showMagnitude && showDuration) {
					icon.text.setText(condition.duration + "x" + condition.magnitude);
				} else if (showDuration) {
					icon.text.setText(condition.duration);
				} else if (showMagnitude) {
					icon.text.setText("x" + condition.magnitude);
				}*/
				text.setText(Integer.toString(condition.magnitude));
				text.setVisibility(View.VISIBLE);
			} else {
				text.setVisibility(View.GONE);
			}
			/*
			if (condition.magnitude != 1) {
				icon.magnitude.setText(Integer.toString(condition.magnitude));
				icon.magnitude.setVisibility(View.VISIBLE);
			} else {
				icon.magnitude.setVisibility(View.GONE);
			}
			if (condition.isTemporaryEffect()) {
				icon.duration.setText(Integer.toString(condition.duration));
				icon.duration.setVisibility(View.VISIBLE);
			} else {
				icon.duration.setVisibility(View.GONE);
			}
			*/
		}
		
		public void hide(boolean useAnimation) {
			if (useAnimation) {
				image.startAnimation(onRemovedIconAnimation);
			} else {
				image.setVisibility(View.GONE);
				condition = null;
			}
			text.setVisibility(View.GONE);
		}
		public void show() {
			image.startAnimation(onNewIconAnimation);
			if (text.getVisibility() == View.VISIBLE) text.startAnimation(onNewIconAnimation);
		}
		
		public void pulseAnimate() {
			image.startAnimation(onAppliedEffectAnimation);
		}
		
		public boolean isVisible() {
			return condition != null;
		}
		
		@Override
		public void onAnimationEnd(Animation animation) { 
			if (animation == this.onRemovedIconAnimation) {
				hide(false);
				rearrangeIconsLeftOf(this);
			}
		}
		
		@Override public void onAnimationRepeat(Animation animation) { }
		@Override public void onAnimationStart(Animation animation) { }
	}
	
	protected void rearrangeIconsLeftOf(ActiveConditionIcon icon) {
		int i = currentConditionIcons.indexOf(icon);
		currentConditionIcons.remove(i);
		currentConditionIcons.add(icon);
		for(; i < currentConditionIcons.size(); ++i) {
			ActiveConditionIcon aci = currentConditionIcons.get(i);
			aci.image.setLayoutParams(getLayoutParamsForIconIndex(i));
		}
	}

	@Override
	public void onActorConditionAdded(Actor actor, ActorCondition condition) {
		ActiveConditionIcon icon = getFirstFreeIcon();
		icon.setActiveCondition(condition);
		icon.show();
	}

	@Override
	public void onActorConditionRemoved(Actor actor, ActorCondition condition) {
		ActiveConditionIcon icon = getIconFor(condition);
		if (icon == null) return;
		icon.hide(true);
	}

	@Override
	public void onActorConditionDurationChanged(Actor actor, ActorCondition condition) {
		ActiveConditionIcon icon = getIconFor(condition);
		if (icon == null) return;
		icon.setIconText();
	}

	@Override
	public void onActorConditionMagnitudeChanged(Actor actor, ActorCondition condition) {
		ActiveConditionIcon icon = getIconFor(condition);
		if (icon == null) return;
		icon.setIconText();
	}

	@Override
	public void onActorConditionRoundEffectApplied(Actor actor, ActorCondition condition) {
		ActiveConditionIcon icon = getIconFor(condition);
		if (icon == null) return;
		icon.pulseAnimate();
	}

	public void unsubscribe(final WorldContext world) {
		world.model.player.conditionListener.remove(this);
		hideAllIcons();
	}

	public void subscribe(final WorldContext world) {
		hideAllIcons();
		for (ActorCondition condition : world.model.player.conditions) {
			getFirstFreeIcon().setActiveCondition(condition);
		}
		world.model.player.conditionListener.add(this);
	}
	
	private void hideAllIcons() {
		for (ActiveConditionIcon icon : currentConditionIcons) icon.hide(false);
	}
}
