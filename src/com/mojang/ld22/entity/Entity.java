package com.mojang.ld22.entity;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Random;

import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class Entity implements Externalizable
{
	private static final long serialVersionUID = -6757626125184439001L;
	
	protected final Random random = new Random();
	public int x, y;
	public int xr = 6;
	public int yr = 6;
	public boolean removed;
	public Level level;

	public void render(Screen screen) {
	}

	public void tick() {
	}

	public void remove() {
		removed = true;
	}

	public final void init(Level level) {
		this.level = level;
	}

	public boolean intersects(int x0, int y0, int x1, int y1) {
		return !(x + xr < x0 || y + yr < y0 || x - xr > x1 || y - yr > y1);
	}

	public boolean blocks(Entity e) {
		return false;
	}

	public void hurt(Tile tile, int x, int y, int dmg) {
	}

	public void hurt(Entity entity, int attackDamage, int attackDir)
	{		
	}

	public boolean move(int xa, int ya) {
		if (xa != 0 || ya != 0) {
			boolean stopped = true;
			if (xa != 0 && move2(xa, 0)) stopped = false;
			if (ya != 0 && move2(0, ya)) stopped = false;
			if (!stopped) {
				int xt = x >> 4;
				int yt = y >> 4;
				level.getTile(xt, yt).steppedOn(level, xt, yt, this);
			}
			return !stopped;
		}
		return true;
	}

	protected boolean move2(int xa, int ya) {
		if (xa != 0 && ya != 0) throw new IllegalArgumentException("Move2 can only move along one axis at a time!");

		int xto0 = ((x) - xr) >> 4;
		int yto0 = ((y) - yr) >> 4;
		int xto1 = ((x) + xr) >> 4;
		int yto1 = ((y) + yr) >> 4;

		int xt0 = ((x + xa) - xr) >> 4;
		int yt0 = ((y + ya) - yr) >> 4;
		int xt1 = ((x + xa) + xr) >> 4;
		int yt1 = ((y + ya) + yr) >> 4;
		boolean blocked = false;
		for (int yt = yt0; yt <= yt1; yt++)
			for (int xt = xt0; xt <= xt1; xt++) {
				if (xt >= xto0 && xt <= xto1 && yt >= yto0 && yt <= yto1) continue;
				level.getTile(xt, yt).bumpedInto(level, xt, yt, this);
				if (!level.getTile(xt, yt).mayPass(level, xt, yt, this)) {
					blocked = true;
					return false;
				}
			}
		if (blocked) return false;

		List<Entity> wasInside = level.getEntities(x - xr, y - yr, x + xr, y + yr);
		List<Entity> isInside = level.getEntities(x + xa - xr, y + ya - yr, x + xa + xr, y + ya + yr);
		for (int i = 0; i < isInside.size(); i++) {
			Entity e = isInside.get(i);
			if (e == this) continue;

			e.touchedBy(this);
		}
		isInside.removeAll(wasInside);
		for (int i = 0; i < isInside.size(); i++) {
			Entity e = isInside.get(i);
			if (e == this) continue;

			if (e.blocks(this)) {
				return false;
			}
		}

		x += xa;
		y += ya;
		return true;
	}

	protected void touchedBy(Entity entity) {
	}

	public boolean isBlockableBy(Mob mob) {
		return true;
	}

	public void touchItem(ItemEntity itemEntity) {
	}

	public boolean canSwim() {
		return false;
	}

	public boolean interact(Player player, Item item, int attackDir) {
		if (item != null) {
			return item.interact(player, this, attackDir);
		}
		return false;
	}

	public boolean use(Player player, int attackDir) {
		return false;
	}

	public int getLightRadius() {
		return 0;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		this.removed = in.readBoolean();
		this.x = in.readInt();
		this.xr = in.readInt();
		this.y = in.readInt();
		this.yr = in.readInt();
		// ignoring this.level, entity is loaded by Level
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeBoolean(this.removed);
		out.writeInt(this.x);
		out.writeInt(this.xr);
		out.writeInt(this.y);
		out.writeInt(this.yr);
		// ignoring this.level, entity is loaded by Level
	}

	/**
	 * Returns a Manhattan distance from the given entity.
	 * 
	 * @param entity
	 * @return
	 */
	protected int distanceFrom(Entity entity)
	{
		if (entity == null) {
			return -1;
		}
		return Math.abs(this.x - entity.x) + Math.abs(this.y - entity.y);
	}
}