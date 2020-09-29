package com.bynder.sdk.query.upload;

public enum UploadIntent {

    CREATE_ASSET("create_asset"),
    GENERATE_ASSET_IMAGE_CROP("generate_asset_image_crop"),
    GENERATE_ASSET_VIDEO_CLIP("generate_asset_video_clip"),
    UPLOAD_ACCOUNT_LOGIN_LOGO("upload_account_login_logo"),
    UPLOAD_ACCOUNT_LOGO("upload_account_logo"),
    UPLOAD_BACKGROUND("upload_background"),
    UPLOAD_BRAND_IMAGE("upload_brand_image"),
    UPLOAD_CUSTOM_THUMBNAIL("upload_custom_thumbnail"),
    UPLOAD_MAIN_UPLOADER_ASSET("upload_main_uploader_asset"),
    UPLOAD_METAPROPERTY_OPTION_IMAGE("upload_metaproperty_option_image"),
    UPLOAD_NEWS_IMAGE("upload_news_image"),
    UPLOAD_PROFILE_IMAGE("upload_profile_image"),
    UPLOAD_STYLEGUIDE_CHAPTER_IMAGE("upload_styleguide_chapter_image"),
    UPLOAD_STYLEGUIDE_PAGE_IMAGE("upload_styleguide_page_image"),
    UPLOAD_TRANSFER_FILE("upload_transfer_file"),
    UPLOAD_WATERMARK("upload_watermark"),
    UPLOAD_WORKFLOW_ASSET("upload_workflow_asset");

    private final String value;

    UploadIntent(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
