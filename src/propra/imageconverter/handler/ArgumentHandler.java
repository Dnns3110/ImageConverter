package propra.imageconverter.handler;

public class ArgumentHandler {

    private String inFile;
    private String outFile;

    public ArgumentHandler(String[] args) throws IllegalArgumentException {
        validateArgs(args);

        if (args[0].matches("--input.+\\.(tga|propra)")) {
            this.inFile = args[0].substring(8);
            this.outFile = args[1].substring(9);
        } else {
            this.inFile = args[1].substring(8);
            this.outFile = args[0].substring(9);
        }
    }

    public String getInFile() {
        return inFile;
    }

    public String getOutFile() {
        return outFile;
    }

    private void validateArgs(String[] args) throws IllegalArgumentException {
        // Check Length of arguments, has to be exactly two
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments.");
            //TODO Genauere Fehlerbeschreibung bei falscher Parameteranzahl
        }

        // Basic check for valid format of both parameters
        for (String arg : args) {
            if (!arg.matches("--(in|out)put.+\\.(tga|propra)")) {
                throw new IllegalArgumentException("Wrong arguments.");
                //TODO Bessere Fehlerbeschreibung
            }
        }
    }

    public String getInFileExtension() {
        return this.inFile.substring(this.inFile.lastIndexOf('.') + 1);
    }

    public String getOutFileExtension() {
        return this.outFile.substring(this.outFile.lastIndexOf('.') + 1);
    }

}

