package propra.imageconverter.handler;

import propra.imageconverter.BaseN;
import propra.imageconverter.WorkMode;
import propra.imageconverter.exceptions.ImageConverterIllegalArgumentException;
import propra.imageconverter.exceptions.InvalidEncodingException;

import java.io.File;

/**
 * Argument handler handles the arguments that are passed to the ImageConverter program. This handler is used on one
 * hand to check the passed arguments, if they are valid, and prepares on the other hand the arguments in a format,
 * that is suitable for further processing during the conversion process.
 */
public class ArgumentHandler {

    private File inFile;
    private File outFile;
    private WorkMode workMode;
    private BaseN encoder;

    /**
     * Constructs an Argument Handler that validates commandline arguments passed to the program.
     *
     * @param args Commandline arguments passed to the program.
     * @throws ImageConverterIllegalArgumentException If passed arguments are invalid.
     */
    public ArgumentHandler(String[] args) throws Exception {
        this.processArgs(args);
    }

    /**
     * Get the path that is passed as --input argument to the program.
     *
     * @return path passed as input file.
     */
    public File getInFile() {
        return inFile;
    }

    /**
     * Get the path that is passed as --output argument to the program.
     *
     * @return path passed as output file.
     */
    public File getOutFile() {
        return outFile;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public BaseN getEncoder() {
        return encoder;
    }

    /**
     * Validate whether passed arguments to program ar valid. Checks on one hand for the number of arguments (which
     * should be two) and on the other hand, if the arguments are in an expected format.
     *
     * @param args Arguments passed to the ImageConverter program.
     * @throws ImageConverterIllegalArgumentException If number of arguments if not two, or if arguments have wrong
     *                                                format.
     */
    private void processArgs(String[] args) throws Exception {
        for (String arg : args) {
            String[] splittedArgument = arg.split("=");

            switch (splittedArgument[0]) {
                case "--input":
                    processInput(arg);
                    break;
                case "--output":
                    processOutput(arg);
                    break;
                case "--decode-base-32":
                    processEncodeDecodeBase32(arg, WorkMode.Decode);
                    break;
                case "--encode-base-32":
                    processEncodeDecodeBase32(arg, WorkMode.Encode);
                    break;
                case "--decode-base-n":
                    processEncodeDecodeBaseN(arg, WorkMode.Decode);
                    break;
                case "--encode-base-n":
                    processEncodeDecodeBaseN(arg, WorkMode.Encode);
                    break;
                case "--compression":
                    processCompression(arg);
                    break;
                default:
                    String message = String.format("Unsupported argument used: %s\n%s", arg, this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
            }
        }

        this.validateFiles();
    }

    /**
     * Get usage for ImageConverter program for users to know, how the program should be called.
     *
     * @return String that gets printed to console window and shows the two ways, the program can be called.
     */
    public String getUsage() {
        return "Usage: \tImageConverter --input=<Path to input file> --output=<Path to output file> --compression=rle\n" +
                "  or \tImageConverter --input=<Path to input file> --output=<Path to output file> --compression=uncompressed\n" +
                "  or \tImageConverter --input=<Path to input file> --encode-base-32\n" +
                "  or \tImageConverter --input=<Path to input file> --decode-base-32\n" +
                "  or \tImageConverter --input=<Path to input file> --encode-base-n=<Alphabet>\n" +
                "  or \tImageConverter --input=<Path to input file> --decode-base-n\n" +
                "Note! Order of arguments does not matter.";
    }

    /**
     * Get file extension from --input argument.
     *
     * @return File extension from input file.
     */
    public String getInFileExtension() {
        String fileName = this.inFile.getName();
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * Get file extension from --output argument.
     *
     * @return File extension from output file.
     */
    public String getOutFileExtension() {
        String fileName = this.outFile.getName();
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }


    private void processInput(String arg) throws ImageConverterIllegalArgumentException {
        String[] splittedArgument = arg.split("=");

        if (this.inFile != null) {
            String message = String.format("Used argument --input twice.\n%s\n%s",
                    arg, this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        } else if (splittedArgument.length == 2) {
            this.inFile = new File(splittedArgument[1]);
        } else {
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        }
    }

    private void processOutput(String arg) throws ImageConverterIllegalArgumentException {
        String[] splittedArgument = arg.split("=");

        if (this.outFile != null) {
            String message = String.format("Used argument --output twice.\n%s\n%s",
                    arg, this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        } else if (splittedArgument.length == 2) {
            this.outFile = new File(splittedArgument[1]);
        } else {
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        }
    }

    private void processEncodeDecodeBase32(String arg, WorkMode workMode) throws ImageConverterIllegalArgumentException, InvalidEncodingException {
        String[] splittedArgument = arg.split("=");

        if (this.workMode != null) {
            getWorkModeError(workMode, arg);
        } else if (splittedArgument.length == 1) {
            this.encoder = new BaseN();
            this.workMode = workMode;
        } else {
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        }
    }

    private void processEncodeDecodeBaseN(String arg, WorkMode workMode) throws ImageConverterIllegalArgumentException, InvalidEncodingException {
        String[] splittedArgument = arg.split("=");

        if (this.workMode != null) {
            getWorkModeError(workMode, arg);
        } else if (splittedArgument.length == 2 && workMode == WorkMode.Encode) {
            this.encoder = new BaseN(splittedArgument[1]);
            this.workMode = workMode;
        } else if (splittedArgument.length == 1 && workMode == WorkMode.Decode) {
            final boolean base32Hex = false;
            this.encoder = new BaseN(base32Hex);
            this.workMode = workMode;
        } else {
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        }
    }

    private void processCompression(String arg) throws ImageConverterIllegalArgumentException {
        String[] splittedArgument = arg.split("=");

        if (this.workMode != null) {
            getWorkModeError(WorkMode.ConvertRLE, arg);
        } else if (splittedArgument.length == 2) {
            if (splittedArgument[1].equals("rle")) {
                this.workMode = WorkMode.ConvertRLE;
            } else if (splittedArgument[1].equals("uncompressed")) {
                this.workMode = WorkMode.ConvertUncompressed;
            } else {
                String message = String.format("Unsupported compression used: %s\n%s", arg, this.getUsage());
                throw new ImageConverterIllegalArgumentException(message);
            }
        } else {
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        }
    }

    private void getWorkModeError(WorkMode newWorkMode, String arg) throws ImageConverterIllegalArgumentException {
        if (this.workMode == newWorkMode) {
            String message = String.format("Used same operation twice.\n%s\n%s",
                    arg, this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        } else {
            if (this.workMode == WorkMode.Decode || this.workMode == WorkMode.Encode) {
                if (newWorkMode == WorkMode.Encode || newWorkMode == WorkMode.Decode) {
                    String message = String.format("Cannot encode and decode at the same time.\n%s\n%s",
                            arg, this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                } else {
                    String message = String.format("Cannot encode/decode and convert at the same time.\n%s\n%s",
                            arg, this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                }
            } else {
                if (newWorkMode == WorkMode.Encode || newWorkMode == WorkMode.Decode) {
                    String message = String.format("Cannot encode/decode and convert at the same time.\n%s\n%s",
                            arg, this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                } else {
                    String message = String.format("Cannot convert uncompressed and RLE-compressed at the same time.\n%s\n%s",
                            arg, this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                }
            }
        }
    }

    private void validateFiles() throws ImageConverterIllegalArgumentException {
        if (this.workMode == null) {
            String message = String.format("No operation specified.\n%s", this.getUsage());
            throw new ImageConverterIllegalArgumentException(message);
        }
        switch (this.workMode) {
            case ConvertRLE:
            case ConvertUncompressed:
                if (this.inFile == null) {
                    String message = String.format("No input file specified.\n%s", this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                } else if (this.outFile == null) {
                    String message = String.format("No output file specified.\n%s", this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                } else {
                    if (!this.getInFileExtension().matches("(tga|propra)")) {
                        String message = String.format("Unsupported file format for input. Only *.tga and *.propra are supported.\nGiven format: %s", this.getInFileExtension());
                        throw new ImageConverterIllegalArgumentException(message);
                    } else if (!this.getOutFileExtension().matches("(tga|propra)")) {
                        String message = String.format("Unsupported file format for output. Only *.tga and *.propra are supported.\nGiven format: %s", this.getOutFileExtension());
                        throw new ImageConverterIllegalArgumentException(message);
                    }
                }
                break;
            case Encode:
                if (this.inFile == null) {
                    String message = String.format("No input file specified.\n%s", this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                } else if (this.outFile != null) {
                    String message = String.format("--output not allowed for encoding operation.\n%s", this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                } else {
                    if (!this.getInFileExtension().matches("(tga|propra)")) {
                        String message = String.format("Unsupported file format for input. Only *.tga and *.propra are supported.\nGiven format: %s", this.getInFileExtension());
                        throw new ImageConverterIllegalArgumentException(message);
                    } else {
                        String extension = this.encoder.isBase32Hex() ? ".base-32" : ".base-n";
                        String out = this.inFile.getAbsolutePath().concat(extension);
                        this.outFile = new File(out);
                    }
                }
                break;
            case Decode:
                if (this.inFile == null) {
                    String message = String.format("No input file specified.\n%s", this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                } else if (this.outFile != null) {
                    String message = String.format("--output not allowed for encoding operation.\n%s", this.getUsage());
                    throw new ImageConverterIllegalArgumentException(message);
                } else {
                    if (!this.getInFileExtension().matches("base\\-(n|32)")) {
                        String extension = this.encoder.isBase32Hex() ? ".base-32" : ".base-n";
                        String message = String.format("Unsupported file format for input. Only *%s is supported for this operation.\nGiven format: %s",
                                extension, this.getInFileExtension());
                        throw new ImageConverterIllegalArgumentException(message);
                    } else {
                        String in = this.inFile.getAbsolutePath();
                        String out = in.substring(0, in.lastIndexOf('.'));
                        this.outFile = new File(out);
                    }
                }
                break;
        }
    }
}

