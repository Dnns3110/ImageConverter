package propra.imageconverter.handler;

import propra.imageconverter.exceptions.ImageConverterIllegalArgumentException;

/**
 * Argument handler handles the arguments that are passed to the ImageConverter program. This handler is used on one
 * hand to check the passed arguments, if they are valid, and prepares on the other hand the arguments in a format,
 * that is suitable for further processing during the conversion process.
 */
public class ArgumentHandler {

    private String inFile;
    private String outFile;

    /**
     * Constructs an Argument Handler that validates commandline arguments passed to the program.
     *
     * @param args Commandline arguments passed to the program.
     * @throws ImageConverterIllegalArgumentException If passed arguments are invalid.
     */
    public ArgumentHandler(String[] args) throws ImageConverterIllegalArgumentException {
        validateArgs(args);

        if (args[0].startsWith("--input")) {
            this.inFile = args[0].substring(8);
            this.outFile = args[1].substring(9);
        } else {
            this.inFile = args[1].substring(8);
            this.outFile = args[0].substring(9);
        }
    }

    /**
     * Get the path that is passed as --input argument to the program.
     *
     * @return path passed as input file.
     */
    public String getInFile() {
        return inFile;
    }

    /**
     * Get the path that is passed as --output argument to the program.
     *
     * @return path passed as output file.
     */
    public String getOutFile() {
        return outFile;
    }

    /**
     * Validate whether passed arguments to program ar valid. Checks on one hand for the number of arguments (which
     * should be two) and on the other hand, if the arguments are in an expected format.
     *
     * @param args Arguments passed to the ImageConverter program.
     * @throws ImageConverterIllegalArgumentException If number of arguments if not two, or if arguments have wrong
     *                                                format.
     */
    private void validateArgs(String[] args) throws ImageConverterIllegalArgumentException {
        // Check Length of arguments, has to be exactly two
        if (args.length != 2) {
            String message = String.format("Wrong number of arguments. 2 arguments expected, got %d arguments.\n%s",
                    args.length, getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        }

        // Basic check for valid format of both parameters
        for (String arg : args) {
            if (!arg.matches("--(in|out)put.+")) {
                throw new ImageConverterIllegalArgumentException("Wrong arguments.\n" + getUsage());
            } else if (!arg.matches(".+\\.(tga|propra)")) {
                throw new ImageConverterIllegalArgumentException("Unsupported file format. Only *.tga and *.propra are supported");
            }
        }
    }

    /**
     * Get usage for ImageConverter program for users to know, how the program should be called.
     *
     * @return String that gets printed to console window and shows the two ways, the program can be called.
     */
    public String getUsage() {
        return "Usage: \tImageConverter --input=<Path to input file> --output=<Path to output file>\n" +
                "  or \tImageConverter --output=<Path to output file> --input=<Path to input file>";
    }

    /**
     * Get file extension from --input argument.
     *
     * @return File extension from input file.
     */
    public String getInFileExtension() {
        return this.inFile.substring(this.inFile.lastIndexOf('.') + 1);
    }

    /**
     * Get file extension from --output argument.
     * @return File extension from output file.
     */
    public String getOutFileExtension() {
        return this.outFile.substring(this.outFile.lastIndexOf('.') + 1);
    }

}

