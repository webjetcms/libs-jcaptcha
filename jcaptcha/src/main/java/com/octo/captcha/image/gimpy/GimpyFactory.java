/*
 * JCaptcha, the open source java framework for captcha definition and integration
 * Copyright (c)  2007 jcaptcha.net. All Rights Reserved.
 * See the LICENSE.txt file distributed with this package.
 */

package com.octo.captcha.image.gimpy;

import com.octo.captcha.CaptchaException;
import com.octo.captcha.CaptchaQuestionHelper;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.image.ImageCaptcha;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

/**
 * Factories for Gimpies. Built on top of WordGenerator and WordToImage. It uses thoses interfaces to build an
 * ImageCaptha answered by a String and for which the question is : Spell the word.
 *
 * WebJET CMS: trieda upravena ak by v slovniku neexistovalo slovo vygenerovanej dlzky, skusa to znova, kym toto
 * slovo nedostane + failsafe aby sa to nezamotalo...
 */
public class GimpyFactory extends com.octo.captcha.image.ImageCaptchaFactory {

    private Random myRandom = new SecureRandom();
    private WordToImage wordToImage;
    private WordGenerator wordGenerator;
    public static final String BUNDLE_QUESTION_KEY = Gimpy.class.getName();

    public GimpyFactory(WordGenerator generator, WordToImage word2image) {
    	if (word2image == null) {
            throw new CaptchaException("Invalid configuration" +
                    " for a GimpyFactory : WordToImage can't be null");
        }
        if (generator == null) {
            throw new CaptchaException("Invalid configuration" +
                    " for a GimpyFactory : WordGenerator can't be null");
        }
        wordToImage = word2image;
        wordGenerator = generator;

    }

    /**
     * gimpies are ImageCaptcha
     *
     * @return the image captcha with default locale
     */
    @Override
	public ImageCaptcha getImageCaptcha() {
        return getImageCaptcha(Locale.getDefault());
    }

    public WordToImage getWordToImage() {
        return wordToImage;
    }

    public WordGenerator getWordGenerator() {
        return wordGenerator;
    }

    /**
     * gimpies are ImageCaptcha
     * WebJET CMS: pridana kontrola ze v pripade slovnikoveho generovania ak chyba slovo danej dlzky skusa generovat znova
     *
     * @return a pixCaptcha with the question :"spell the word"
     */
    @Override
	public ImageCaptcha getImageCaptcha(Locale locale) {

    	String word=null;
    	int failSafeCounter=0;
    	while (word==null && failSafeCounter++<30)
    	{
	    	try
	    	{
		        Integer wordLength = getRandomLength();
		        word = getWordGenerator().getWord(wordLength, locale);
	    	}
	    	catch (CaptchaException ex)
	    	{
	    		word=null;
	    	}
    	}
    	if (word==null)
    		throw new CaptchaException("Nepodarilo sa vygenerovat text pre Captcha.");
        BufferedImage image = null;
        try {
            image = getWordToImage().getImage(word);
        } catch (Throwable e) {
            throw new CaptchaException(e);
        }

        ImageCaptcha captcha = new Gimpy(CaptchaQuestionHelper.getQuestion(locale, BUNDLE_QUESTION_KEY), image, word);
        return captcha;
    }

    protected Integer getRandomLength() {
        Integer wordLength;
        int range = getWordToImage().getMaxAcceptedWordLength() -
                getWordToImage().getMinAcceptedWordLength();
        int randomRange = range != 0 ? myRandom.nextInt(range + 1) : 0;
        wordLength = Integer.valueOf(randomRange +
                getWordToImage().getMinAcceptedWordLength());
      return wordLength;
    }

}
