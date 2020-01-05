package propra.imageconverter.handler;

import propra.imageconverter.BaseN;
import propra.imageconverter.WorkMode;
import propra.imageconverter.exceptions.IllegalArgumentException;
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
     * @throws IllegalArgumentException If passed arguments are invalid.
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

    /**
     * Set the path used for image conversion (usually used for auto compression).
     *
     * @param outFile out file to be set afterwards (so far only used for auto compression).
     */
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    /**
     * Get the WorkMode identified from the given arguments.
     *
     * @return workmode
     */
    public WorkMode getWorkMode() {
        return workMode;
    }

    /**
     * Set the WorkMode used for running the image converter (usually used for auto compression).
     *
     * @param workMode Work Mode to be set (so far only used for auto compression mode).
     */
    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    /**
     * Returns an Encoder, that gets created based on the given decode/encode arguments.
     * Can be a Base-32 Hex encoder, or a generic Base-N encoder with custom alphabet.
     *
     * @return encoder.
     */
    public BaseN getEncoder() {
        return encoder;
    }

    /**
     * Validate whether passed arguments to program ar valid. Checks on one hand for the number of arguments (which
     * should be two) and on the other hand, if the arguments are in an expected format.
     *
     * @param args Arguments passed to the ImageConverter program.
     * @throws Exception if arguments are invalid.
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
                    throw new IllegalArgumentException(message);
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
                "  or \tImageConverter --input=<Path to input file> --output=<Path to output file> --compression=auto\n" +
                "  or \tImageConverter --input=<Path to input file> --output=<Path to output file in *.propra format> --compression=huffman\n" +
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

    /**
     * Processes --input= argument
     *
     * @param arg String that starts with "--input=" followed by a file path.
     * @throws IllegalArgumentException if --input argument is used wrong.
     */
    private void processInput(String arg) throws IllegalArgumentException {
        String[] splittedArgument = arg.split("=");

        if (this.inFile != null) {
            // Show error, if --input is used twice.
            String message = String.format("Used argument --input twice.\n%s\n%s",
                    arg, this.getUsage());
            throw new IllegalArgumentException(message);
        } else if (splittedArgument.length == 2) {
            this.inFile = new File(splittedArgument[1]);
        } else {
            // Show error, if there is no path given after = sign, or = is used more than once in that argument.
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Processes --output= argument
     *
     * @param arg String that starts with "--output=" followed by a file path.
     * @throws IllegalArgumentException if --output argument is used wrong.
     */
    private void processOutput(String arg) throws IllegalArgumentException {
        String[] splittedArgument = arg.split("=");

        if (this.outFile != null) {
            // Show error, if --output is used twice.
            String message = String.format("Used argument --output twice.\n%s\n%s",
                    arg, this.getUsage());
            throw new IllegalArgumentException(message);
        } else if (splittedArgument.length == 2) {
            this.outFile = new File(splittedArgument[1]);
        } else {
            // Show error, if there is no path given after = sign, or = is used more than once in that argument.
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Processes --encode-base-32 and --decode-base-32 argument
     *
     * @param arg      String "--encode-base-32" or "--decode-base-32".
     * @param workMode work mode to be set
     * @throws IllegalArgumentException if encode/decode argument is used wrong.
     * @throws InvalidEncodingException if the encoding to be set is invalid.
     */
    private void processEncodeDecodeBase32(String arg, WorkMode workMode) throws IllegalArgumentException, InvalidEncodingException {
        String[] splittedArgument = arg.split("=");

        if (this.workMode != null) {
            // Show error, if there is already a workmode specified.
            getWorkModeError(workMode, arg);
        } else if (arg.matches("--(en|de)code-base-32")) {
            this.encoder = new BaseN();
            this.workMode = workMode;
        } else {
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Processes --encode-base-n and --decode-base-n argument
     *
     * @param arg      String "--decode-base-n" or one that starts with "--encode-base-n=", followed by the alphabet.
     * @param workMode work mode to be set
     * @throws IllegalArgumentException if encode/decode argument is used wrong.
     * @throws InvalidEncodingException if the encoding to be set is invalid.
     */
    private void processEncodeDecodeBaseN(String arg, WorkMode workMode) throws IllegalArgumentException, InvalidEncodingException {
        String[] splittedArgument = arg.split("=");

        if (this.workMode != null) {
            // Show error, if there is already a workmode specified.
            getWorkModeError(workMode, arg);
        } else if (splittedArgument.length == 2 && workMode == WorkMode.Encode) {
            this.encoder = new BaseN(splittedArgument[1]);
            this.workMode = workMode;
        } else if (arg.equals("--decode-base-n") && workMode == WorkMode.Decode) {
            final boolean base32Hex = false;
            this.encoder = new BaseN(base32Hex);
            this.workMode = workMode;
        } else {
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Processes --compression= argument.
     *
     * @param arg String that starts with "--compression=" followed by "uncompressed" or "rle.
     * @throws IllegalArgumentException if --compression argument is used wrong.
     */
    private void processCompression(String arg) throws IllegalArgumentException {
        String[] splittedArgument = arg.split("=");

        if (this.workMode != null) {
            getWorkModeError(WorkMode.ConvertRLE, arg);
        } else if (splittedArgument.length == 2) {
            switch (splittedArgument[1]) {
                case "rle":
                    this.workMode = WorkMode.ConvertRLE;
                    break;
                case "uncompressed":
                    this.workMode = WorkMode.ConvertUncompressed;
                    break;
                case "huffman":
                    this.workMode = WorkMode.ConvertHuffman;
                    break;
                case "auto":
                    this.workMode = WorkMode.ConvertAuto;
                    break;
                default:
                    String message = String.format("Unsupported compression used: %s\n%s", arg, this.getUsage());
                    throw new IllegalArgumentException(message);
            }
        } else {
            String message = String.format("Wrong use of argument %s: %s\n%s",
                    splittedArgument[0], arg, this.getUsage());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Returns an error based on the already set work Mode, and the other work mode, that should have been set.
     *
     * @param newWorkMode work mode that should have been set.
     * @param arg         current processed argument
     * @throws IllegalArgumentException the error that should be shown.
     */
    private void getWorkModeError(WorkMode newWorkMode, String arg) throws IllegalArgumentException {
        if (this.workMode == newWorkMode) {
            String message = String.format("Used same operation twice.\n%s\n%s",
                    arg, this.getUsage());
            throw new IllegalArgumentException(message);
        } else {
            if (this.workMode == WorkMode.Decode || this.workMode == WorkMode.Encode) {
                if (newWorkMode == WorkMode.Encode || newWorkMode == WorkMode.Decode) {
                    String message = String.format("Cannot encode and decode at the same time.\n%s\n%s",
                            arg, this.getUsage());
                    throw new IllegalArgumentException(message);
                } else {
                    String message = String.format("Cannot encode/decode and convert at the same time.\n%s\n%s",
                            arg, this.getUsage());
                    throw new IllegalArgumentException(message);
                }
            } else {
                if (newWorkMode == WorkMode.Encode || newWorkMode == WorkMode.Decode) {
                    String message = String.format("Cannot encode/decode and convert at the same time.\n%s\n%s",
                            arg, this.getUsage());
                    throw new IllegalArgumentException(message);
                } else {
                    String message = String.format("Cannot convert uncompressed and RLE-compressed at the same time.\n%s\n%s",
                            arg, this.getUsage());
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

    /**
     * Validate given input files based on the identified work mode.
     *
     * @throws IllegalArgumentException if the files are not specified correctly for the chosen work mode.
     */
    private void validateFiles() throws IllegalArgumentException {
        if (this.workMode == null) {
            String message = String.format("No operation specified.\n%s", this.getUsage());
            throw new IllegalArgumentException(message);
        }
        switch (this.workMode) {
            case ConvertRLE:
            case ConvertUncompressed:
            case ConvertHuffman:
            case ConvertAuto:
                if (this.inFile == null) {
                    String message = String.format("No input file specified.\n%s", this.getUsage());
                    throw new IllegalArgumentException(message);
                } else if (this.outFile == null) {
                    String message = String.format("No output file specified.\n%s", this.getUsage());
                    throw new IllegalArgumentException(message);
                } else {
                    if (!this.getInFileExtension().matches("(tga|propra)")) {
                        String message = String.format("Unsupported file format for input. Only *.tga and *.propra are supported.\nGiven format: %s", this.getInFileExtension());
                        throw new IllegalArgumentException(message);
                    } else if (!this.getOutFileExtension().matches("(tga|propra)")) {
                        String message = String.format("Unsupported file format for output. Only *.tga and *.propra are supported.\nGiven format: %s", this.getOutFileExtension());
                        throw new IllegalArgumentException(message);
                    } else if (this.workMode == WorkMode.ConvertHuffman && !this.getOutFileExtension().equals("propra")) {
                        String message = String.format("Unsupported file format for output when using huffman compression. Only *.propra is supported.\nGiven format: %s", this.getOutFileExtension());
                        throw new IllegalArgumentException(message);
                    }
                }
                break;
            case Encode:
                if (this.inFile == null) {
                    String message = String.format("No input file specified.\n%s", this.getUsage());
                    throw new IllegalArgumentException(message);
                } else if (this.outFile != null) {
                    String message = String.format("--output not allowed for encoding operation.\n%s", this.getUsage());
                    throw new IllegalArgumentException(message);
                } else {
                    if (!this.getInFileExtension().matches("(tga|propra)")) {
                        String message = String.format("Unsupported file format for input. Only *.tga and *.propra are supported.\nGiven format: %s", this.getInFileExtension());
                        throw new IllegalArgumentException(message);
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
                    throw new IllegalArgumentException(message);
                } else if (this.outFile != null) {
                    String message = String.format("--output not allowed for encoding operation.\n%s", this.getUsage());
                    throw new IllegalArgumentException(message);
                } else {
                    if (!this.getInFileExtension().matches("base\\-(n|32)")) {
                        String extension = this.encoder.isBase32Hex() ? ".base-32" : ".base-n";
                        String message = String.format("Unsupported file format for input. Only *%s is supported for this operation.\nGiven format: %s",
                                extension, this.getInFileExtension());
                        throw new IllegalArgumentException(message);
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

