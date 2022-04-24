/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.entity;

import android.text.TextUtils;
import android.util.Log;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.util.help.UTF8BOMFighter;
import xyz.fycz.myreader.util.utils.EncodingDetect;
import xyz.fycz.myreader.util.utils.FileUtils;
import xyz.fycz.myreader.util.utils.StringUtils;

/**
 * @author fengyue
 * @date 2021/5/14 12:05
 */
public class StrResponse {
    private String encodeType;
    private Response response;
    private String body;

    public String getEncodeType() {
        return encodeType;
    }

    public void setEncodeType(String encodeType) {
        this.encodeType = encodeType;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String body() {
        if (body == null) {
            if (response != null) {
                ResponseBody body = response.body();
                if (body != null) {
                    try {
                        byte[] responseBytes = UTF8BOMFighter.removeUTF8BOM(body.bytes());
                        if (!TextUtils.isEmpty(encodeType)) {
                            try {
                                this.body = new String((responseBytes), Charset.forName(encodeType));
                                Log.d("Http: read finish", this.body);
                                return this.body;
                            } catch (Exception ignored) {
                            }
                        }
                        String charsetStr;
                        MediaType mediaType = body.contentType();
                        //根据http头判断
                        if (mediaType != null) {
                            Charset charset = mediaType.charset();
                            if (charset != null) {
                                this.body = new String((responseBytes), charset);
                                Log.d("Http: read finish", this.body);
                                return this.body;
                            }
                        }
                        //根据内容判断
                        charsetStr = EncodingDetect.getEncodeInHtml(responseBytes);
                        this.body = new String(responseBytes, Charset.forName(charsetStr));
                        Log.d("Http: read finish", this.body);
                        return this.body;
                    } catch (IOException e) {
                        e.printStackTrace();
                        this.body = "";
                    }
                }
            } else {
                this.body = "";
            }
        }
        return this.body;
    }
}
