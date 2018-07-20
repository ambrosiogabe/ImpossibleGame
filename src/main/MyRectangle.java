package main;

import java.awt.Color;
import main.MyRectangle;

public class MyRectangle {
	public int width;
	public int height;
	public int x, y, dx, dy, id, maxY, maxX, minY, minX, i, j;
	public Color color;
	
	public MyRectangle(int setWidth, int setHeight, int setx, int sety) {
		width = setWidth;
		height = setHeight;
		x = setx;
		y = sety;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int centerx() {
		int centerx = this.x + (this.width / 2);
		return centerx;
	}
	
	public int centery() {
		int centery = this.y + (this.height / 2);
		return centery;
	}
	
	public int halfWidth() {
		int halfWidth = width / 2;
		return halfWidth;
	}
	
	public int halfHeight() {
		int halfHeight = height / 2;
		return halfHeight;
	}
	
	public int blockRectangle(MyRectangle r1, MyRectangle r2) {
		//For collision side 1 = 'top'   2 = 'bottom'   3 = 'left'   4 = 'right'
		int collisionSide, overlapX, overlapY;
		
		int vx = r1.centerx() - r2.centerx();
		int vy = r1.centery() - r2.centery();
		
		int combinedHalfWidths = r1.halfWidth() + r2.halfWidth();
		int combinedHalfHeights = r1.halfHeight() + r2.halfHeight();
		
		if(Math.abs(vx) < combinedHalfWidths) {
			if(Math.abs(vy) < combinedHalfHeights) {
				overlapX = combinedHalfWidths - Math.abs(vx);
				overlapY = combinedHalfHeights - Math.abs(vy);
				
				if(overlapX >= overlapY) {
					if(vy > 0) {
						collisionSide = 1;
						r1.y += overlapY;
					} else {
						collisionSide = 2;
						r1.y -= overlapY;
					}
				} else {
					if(vx > 0) {
						collisionSide = 3;
						r1.x += overlapX;
					} else {
						collisionSide = 4;
						r1.x -= overlapX;
					}
				}
			} else {
				collisionSide = 0;
			}
		} else {
			collisionSide = 0;
		}
		
		return collisionSide;
	}


}
