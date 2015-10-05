package com.tripgallery.util;

import com.kbeanie.imagechooser.api.ChosenImage;

/**
 * @author Victor
 */
public interface ImageSelectedCallback
{
	public void handleImage(ChosenImage image);
}
