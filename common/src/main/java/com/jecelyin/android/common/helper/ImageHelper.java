package com.jecelyin.android.common.helper;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jecelyin.android.common.R;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class ImageHelper {

    public static Target<GlideDrawable> load(Context context, String imageUrl, final ImageView imageView) {
        return load(context, imageUrl, imageView, null);
    }

    public static Target<GlideDrawable> load(Context context, String imageUrl, ImageView imageView, Callback callback) {
        return load(context, imageUrl, imageView, callback, true);
    }

    public static Target<GlideDrawable> load(Context context, String imageUrl, ImageView imageView, int errorResId) {
        return load(context, imageUrl, imageView, null, true, R.drawable.image_loading, errorResId);
    }

    public static Target<GlideDrawable> load(Context context, String imageUrl, ImageView imageView, Callback callback, boolean fit) {
        return load(context, imageUrl, imageView, callback, true, R.drawable.image_loading, R.drawable.image_loading_error);
    }

    public static Target<GlideDrawable> load(Context context, String imageUrl, ImageView imageView, Callback callback, boolean fit, int loadingResId, int errorResId) {
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(errorResId);
            return null;
        }

        ImageCallback imageCallback = new ImageCallback(imageView, callback) {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                if (getCallback() != null)
                    getCallback().onError();
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                if (getCallback() != null)
                    getCallback().onSuccess();
                return false;
            }
        };

        //picasso 会旋转图片，e.g. IOS横屏拍的照片
        DrawableRequestBuilder<String> creator = Glide.with(context).load(imageUrl)    //可以是本地图片或网络图片
                .placeholder(loadingResId)   //当图片正在加载时显示的图片(optional)
                .error(errorResId);           //当图片加载失败时显示的图片(optional)
        if(fit)
            creator.centerCrop();

        creator.listener(imageCallback);
        return creator.into(imageView);
    }

    public static void cancelRequest(ImageView iv) {
        Glide.clear(iv);
    }

    public interface Callback {
        void onSuccess();

        void onError();
    }

    private static abstract class ImageCallback implements RequestListener<String, GlideDrawable> {
        private final Callback callback;
        private final ImageView imageView;

        public ImageCallback(ImageView imageView, Callback callback) {
            this.imageView = imageView;
            this.callback = callback;
        }

        public Callback getCallback() {
            return callback;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }
}
