package propra.imageconverter;

public interface Image {

    Image convert();

    void save(String filePath);
}
