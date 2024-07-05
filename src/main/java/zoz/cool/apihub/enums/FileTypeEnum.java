package zoz.cool.apihub.enums;


public enum FileTypeEnum {
    //image/jpeg,image/png,image/bmp,image/webp,application/pdf
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_BMP("image/bmp"),
    IMAGE_WEBP("image/webp"),
    PDF("application/pdf");

    private final String name;

    FileTypeEnum(String name) {
        this.name = name;
    }

    public static FileTypeEnum getFileType(String name) {
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (fileTypeEnum.name.equals(name)) {
                return fileTypeEnum;
            }
        }
        return null;
    }

    public static String[] getAllTypes() {
        String[] types = new String[FileTypeEnum.values().length];
        for (int i = 0; i < FileTypeEnum.values().length; i++) {
            types[i] = FileTypeEnum.values()[i].name();
        }
        return types;
    }
}
