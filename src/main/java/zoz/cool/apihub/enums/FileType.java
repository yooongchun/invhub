package zoz.cool.apihub.enums;


public enum FileType {
    //image/jpeg,image/png,image/bmp,image/webp,application/pdf
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_BMP("image/bmp"),
    IMAGE_WEBP("image/webp"),
    PDF("application/pdf");

    private final String name;

    FileType(String name) {
        this.name = name;
    }

    public static FileType getFileType(String name) {
        for (FileType fileType : FileType.values()) {
            if (fileType.name.equals(name)) {
                return fileType;
            }
        }
        return null;
    }

    public static String[] getAllTypes() {
        String[] types = new String[FileType.values().length];
        for (int i = 0; i < FileType.values().length; i++) {
            types[i] = FileType.values()[i].name();
        }
        return types;
    }
}
