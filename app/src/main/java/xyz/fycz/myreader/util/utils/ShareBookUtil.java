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

package xyz.fycz.myreader.util.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.entity.SharedBook;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.util.IOUtils;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2021/6/3 21:46
 */
public class ShareBookUtil {
    public static void shareBook(Context context, Book mBook, ImageView cover) {
        StoragePermissionUtils.request(context, (permissions, all) -> {
            shareBook2(context, mBook, cover);
        });
    }

    /**
     * 分享书籍
     */
    public static void shareBook2(Context context, Book mBook, ImageView cover) {
        if ("本地书籍".equals(mBook.getType())) {
            File file = new File(mBook.getChapterUrl());
            if (!file.exists()) {
                ToastUtils.showWarring("书籍源文件不存在，无法分享！");
                return;
            }
            try {
                ShareUtils.share(context, file, mBook.getName() + ".txt", "text/plain");
            } catch (Exception e) {
                String dest = APPCONST.SHARE_FILE_DIR + File.separator + mBook.getName() + ".txt";
                FileUtils.copy(mBook.getChapterUrl(), dest);
                ShareUtils.share(context, new File(dest), mBook.getName() + ".txt", "text/plain");
            }
            return;
        }
        ToastUtils.showInfo("正在生成分享图片");
        Single.create((SingleOnSubscribe<File>) emitter -> {
            // 使用url
            String url = SharedPreUtils.getInstance().getString(context.getString(R.string.downloadLink), URLCONST.LAN_ZOU_URL);
            if (url == null)
                url = "";

            int maxLength = 1273 - 1 - url.length();

            SharedBook sharedBook = SharedBook.bookToSharedBook(mBook);

            url = url + "#" + GsonExtensionsKt.getGSON().toJson(sharedBook);

            Log.d("QRcode", "Length=" + url.length() + "\n" + url);

            Bitmap bitmap;
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            bitmap = QRCodeEncoder.syncEncodeQRCode(url, 360);
            QRCodeEncoder.HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            File share = makeShareFile(context, mBook, cover, bitmap);
            if (share == null) {
                ToastUtils.showError("分享图片生成失败");
                return;
            }
            emitter.onSuccess(share);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new MySingleObserver<File>() {
                    @Override
                    public void onSuccess(@NonNull File File) {
                        share(context, File);
                    }
                });
    }

    /**
     * 生成分享图片
     *
     * @param QRCode
     * @return
     */

    private static File makeShareFile(Context context, Book mBook, ImageView cover, Bitmap QRCode) {
        FileOutputStream fos = null;
        try {
            Bitmap back = BitmapFactory.decodeStream(context.getResources().getAssets().open("share.png")).copy(Bitmap.Config.ARGB_8888, true);
            int backWidth = back.getWidth();
            int backHeight = back.getHeight();

            int margin = 60;

            int marginTop = 24;

            cover.setDrawingCacheEnabled(true);
            Bitmap img = Bitmap.createBitmap(cover.getDrawingCache()).copy(Bitmap.Config.ARGB_8888, true);
            cover.setDrawingCacheEnabled(false);
            img = BitmapUtil.getBitmap(img, 152, 209);

            Canvas cv = new Canvas(back);
            cv.drawBitmap(img, margin, margin + marginTop * 2, null);

            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setFilterBitmap(true);
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(40);

            String name = TextUtils.ellipsize(mBook.getName(), textPaint, backWidth - margin + marginTop * 3 - img.getWidth(), TextUtils.TruncateAt.END).toString();
            cv.drawText(name, margin + marginTop + img.getWidth(), margin + marginTop * 4, textPaint);


            textPaint.setColor(context.getResources().getColor(R.color.origin));
            textPaint.setTextSize(32);
            cv.drawText(mBook.getAuthor(), margin + marginTop + img.getWidth(), margin + marginTop * 6, textPaint);

            textPaint.setColor(Color.BLACK);
            cv.drawText(mBook.getType() == null ? "" : mBook.getType(), margin + marginTop + img.getWidth(), margin + marginTop * 8, textPaint);
            cv.drawText("书源：" + BookSourceManager.getSourceNameByStr(mBook.getSource()), margin + marginTop + img.getWidth(), margin + marginTop * 10, textPaint);

            int textSize = 35;
            int textInterval = textSize / 2;
            textPaint.setTextSize(textSize);

            drawDesc(getDescLines(mBook, backWidth - margin * 2, textPaint), textPaint, cv, margin + marginTop * 4 + img.getHeight(), margin, textInterval);

            cv.drawBitmap(QRCode, backWidth - QRCode.getWidth(), backHeight - QRCode.getHeight(), null);

            cv.save();// 保存
            cv.restore();// 存储

            File share = FileUtils.getFile(APPCONST.SHARE_FILE_DIR + mBook.getName() + "_share.png");
            fos = new FileOutputStream(share);
            back.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Log.i("tag", "saveBitmap success: " + share.getAbsolutePath());

            back.recycle();
            img.recycle();
            QRCode.recycle();

            return share;
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showError(e.getLocalizedMessage() + "");
            return null;
        } finally {
            IOUtils.close(fos);
        }
    }

    /**
     * 分享生成的图片
     *
     * @param share
     */
    private static void share(Context context, File share) {
        ShareUtils.share(context, share, "分享书籍", "image/png");
    }

    /**
     * 绘制简介
     *
     * @param lines
     * @param textPaint
     * @param canvas
     * @param top
     * @param left
     * @param textInterval
     */
    private static void drawDesc(List<String> lines, TextPaint textPaint, Canvas canvas, int top, int left, int textInterval) {
        float interval = textInterval + textPaint.getTextSize();
        for (String line : lines) {
            canvas.drawText(line, left, top, textPaint);
            top += interval;
        }
    }

    /**
     * 生成简介lines
     *
     * @param width
     * @param textPaint
     * @return
     */

    private static List<String> getDescLines(Book mBook, int width, TextPaint textPaint) {
        List<String> lines = new ArrayList<>();
        String desc = StringUtils.halfToFull("  ") + mBook.getDesc();
        int i = 0;
        int wordCount = 0;
        String subStr = null;
        while (desc.length() > 0) {
            if (i == 9) {
                lines.add(TextUtils.ellipsize(desc, textPaint, width / 1.8f, TextUtils.TruncateAt.END).toString());
                break;
            }
            wordCount = textPaint.breakText(desc, true, width, null);
            subStr = desc.substring(0, wordCount);
            lines.add(subStr);
            desc = desc.substring(wordCount);
            i++;
        }
        return lines;
    }
}
