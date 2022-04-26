/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.widget.custom;


import android.content.Context;



import android.util.AttributeSet;


import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatTextView;


public class MyTextView extends /*AppCompatEditText*/ AppCompatTextView {

   /* private OnTouchListener mOnTouchListener;
    private long timeDown;
    private long timeUp;*/


    public MyTextView(Context context) {
        super(context);

    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


   /* @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (timeUp - timeDown > 0 && timeUp - timeDown < 1000){
            return false;
        }else {
            return super.dispatchTouchEvent(event);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            timeDown = System.currentTimeMillis();
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            timeUp = System.currentTimeMillis();
        }
        return super.onTouchEvent(event);
    }*/




}

