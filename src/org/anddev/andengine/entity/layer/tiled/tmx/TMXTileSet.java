package org.anddev.andengine.entity.layer.tiled.tmx;

import org.anddev.andengine.entity.layer.tiled.tmx.util.constants.TMXConstants;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXParseException;
import org.anddev.andengine.opengl.texture.TextureManager;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.bitmap.BitmapTexture;
import org.anddev.andengine.opengl.texture.bitmap.BitmapTexture.TextureFormat;
import org.anddev.andengine.opengl.texture.bitmap.BitmapTextureFactory;
import org.anddev.andengine.opengl.texture.bitmap.source.AssetBitmapTextureSource;
import org.anddev.andengine.opengl.texture.bitmap.source.decorator.ColorKeyBitmapTextureSourceDecorator;
import org.anddev.andengine.opengl.texture.bitmap.source.decorator.shape.RectangleBitmapTextureSourceDecoratorShape;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.util.SAXUtils;
import org.xml.sax.Attributes;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;

/**
 * (c) 2010 Nicolas Gramlich 
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 19:03:24 - 20.07.2010
 */
public class TMXTileSet implements TMXConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final int mFirstGlobalTileID;
	private final String mName;
	private final int mTileWidth;
	private final int mTileHeight;

	private String mImageSource;
	private BitmapTexture mBitmapTexture;
	private final TextureOptions mTextureOptions;

	private int mTilesHorizontal;
	@SuppressWarnings("unused")
	private int mTilesVertical;

	private final int mSpacing;
	private final int mMargin;

	private final SparseArray<TMXProperties<TMXTileProperty>> mTMXTileProperties = new SparseArray<TMXProperties<TMXTileProperty>>();
	// ===========================================================
	// Constructors
	// ===========================================================

	TMXTileSet(final Attributes pAttributes, final TextureOptions pTextureOptions) {
		this(SAXUtils.getIntAttribute(pAttributes, TAG_TILESET_ATTRIBUTE_FIRSTGID, 1), pAttributes, pTextureOptions);
	}

	TMXTileSet(final int pFirstGlobalTileID, final Attributes pAttributes, final TextureOptions pTextureOptions) {
		this.mFirstGlobalTileID = pFirstGlobalTileID;
		this.mName = pAttributes.getValue("", TAG_TILESET_ATTRIBUTE_NAME);
		this.mTileWidth = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_TILESET_ATTRIBUTE_TILEWIDTH);
		this.mTileHeight = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_TILESET_ATTRIBUTE_TILEHEIGHT);
		this.mSpacing = SAXUtils.getIntAttribute(pAttributes, TAG_TILESET_ATTRIBUTE_SPACING, 0);
		this.mMargin = SAXUtils.getIntAttribute(pAttributes, TAG_TILESET_ATTRIBUTE_MARGIN, 0);

		this.mTextureOptions = pTextureOptions;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public final int getFirstGlobalTileID() {
		return this.mFirstGlobalTileID;
	}

	public final String getName() {
		return this.mName;
	}

	public final int getTileWidth() {
		return this.mTileWidth;
	}

	public final int getTileHeight() {
		return this.mTileHeight;
	}

	public BitmapTexture getTexture() {
		return this.mBitmapTexture;
	}

	public void setImageSource(final Context pContext, final TextureManager pTextureManager, final Attributes pAttributes) throws TMXParseException {
		this.mImageSource = pAttributes.getValue("", TAG_IMAGE_ATTRIBUTE_SOURCE);

		final AssetBitmapTextureSource assetBitmapTextureSource = new AssetBitmapTextureSource(pContext, this.mImageSource);
		this.mTilesHorizontal = TMXTileSet.determineCount(assetBitmapTextureSource.getWidth(), this.mTileWidth, this.mMargin, this.mSpacing);
		this.mTilesVertical = TMXTileSet.determineCount(assetBitmapTextureSource.getHeight(), this.mTileHeight, this.mMargin, this.mSpacing);
		this.mBitmapTexture = BitmapTextureFactory.createForTextureSourceSize(TextureFormat.RGBA_8888, assetBitmapTextureSource, this.mTextureOptions); // TODO Make TextureFormat variable

		final String transparentColor = SAXUtils.getAttribute(pAttributes, TAG_IMAGE_ATTRIBUTE_TRANS, null);
		if(transparentColor == null) {
			TextureRegionFactory.createFromSource(this.mBitmapTexture, assetBitmapTextureSource, 0, 0, true);
		} else {
			try{
				final int color = Color.parseColor((transparentColor.charAt(0) == '#') ? transparentColor : "#" + transparentColor);
				TextureRegionFactory.createFromSource(this.mBitmapTexture, new ColorKeyBitmapTextureSourceDecorator(assetBitmapTextureSource, RectangleBitmapTextureSourceDecoratorShape.getDefaultInstance(), color), 0, 0, true);
			} catch (final IllegalArgumentException e) {
				throw new TMXParseException("Illegal value: '" + transparentColor + "' for attribute 'trans' supplied!", e);
			}
		}
		pTextureManager.loadTexture(this.mBitmapTexture);
	}

	public String getImageSource() {
		return this.mImageSource;
	}

	public SparseArray<TMXProperties<TMXTileProperty>> getTMXTileProperties() {
		return this.mTMXTileProperties;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public TMXProperties<TMXTileProperty> getTMXTilePropertiesFromGlobalTileID(final int pGlobalTileID) {
		final int localTileID = pGlobalTileID - this.mFirstGlobalTileID;
		return this.mTMXTileProperties.get(localTileID);
	}

	public void addTMXTileProperty(final int pLocalTileID, final TMXTileProperty pTMXTileProperty) {
		final TMXProperties<TMXTileProperty> existingProperties = this.mTMXTileProperties.get(pLocalTileID);
		if(existingProperties != null) {
			existingProperties.add(pTMXTileProperty);
		} else {
			final TMXProperties<TMXTileProperty> newProperties = new TMXProperties<TMXTileProperty>();
			newProperties.add(pTMXTileProperty);
			this.mTMXTileProperties.put(pLocalTileID, newProperties);
		}
	}

	public TextureRegion getTextureRegionFromGlobalTileID(final int pGlobalTileID) {
		final int localTileID = pGlobalTileID - this.mFirstGlobalTileID;
		final int tileColumn = localTileID % this.mTilesHorizontal;
		final int tileRow = localTileID / this.mTilesHorizontal;

		final int texturePositionX = this.mMargin + (this.mSpacing + this.mTileWidth) * tileColumn;
		final int texturePositionY = this.mMargin + (this.mSpacing + this.mTileHeight) * tileRow;

		return new TextureRegion(this.mBitmapTexture, texturePositionX, texturePositionY, this.mTileWidth, this.mTileHeight);
	}

	private static int determineCount(final int pTotalExtent, final int pTileExtent, final int pMargin, final int pSpacing) {
		int count = 0;
		int remainingExtent = pTotalExtent;

		remainingExtent -= pMargin * 2;

		while(remainingExtent > 0) {
			remainingExtent -= pTileExtent;
			remainingExtent -= pSpacing;
			count++;
		}

		return count;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
