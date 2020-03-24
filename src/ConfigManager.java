package noter.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConfigManager {

	private static String CONFIG_FILEPATH = "config.txt";
	private static String DEF_CONFIG_FILEPATH = "config.default";
	private static String CORRUPTED_CONFIG_FILEPATH = "config.corrupted";
	private static File configFile;

	private static boolean isConfigLoaded = false;

	private static HashMap<String, String> configVariablesMap = new HashMap<>();
	private static HashMap<String, String> keyBindingsMap = new HashMap<>();

	public static void init(){
		configFile = getConfigFileRef(CONFIG_FILEPATH);

		try {
			if (configFile.exists()) {
				if (configFile.canRead()) {
					loadConfigFile(configFile);
				}else {
					createBkpAndLoadDefaultConfig();
				}
			}else {
				loadFromDefaults();
			}
		}catch (IOException e){
			e.printStackTrace();
			System.exit(1); // TODO - Error Handling
		}
	}

	private static File getConfigFileRef(String filePath){
		File tmp = new File(filePath);
		return tmp;
	}

	private static void createBkpAndLoadDefaultConfig() throws IOException{
		File corruptedConfigFile = getConfigFileRef(System.nanoTime() + "-" + CORRUPTED_CONFIG_FILEPATH);
		configFile.renameTo(corruptedConfigFile);
		loadFromDefaults();
	}

	private static void loadFromDefaults() throws IOException{
		File defConfigFile = getConfigFileRef(DEF_CONFIG_FILEPATH);
		if(defConfigFile.exists() && defConfigFile.canRead()){
			Files.copy(defConfigFile.toPath(), configFile.toPath());
			loadConfigFile(configFile);
		} else{
			System.err.println("[" + Instant.now().toString() + "][FATAL] Config Error - No default config found");
			System.exit(2); // TODO - Error Handling
		}
	}

	private static void loadConfigFile(File configFile) throws FileNotFoundException {
		Scanner fileScanner = new Scanner(configFile);
		String line;
		while (fileScanner.hasNextLine()){
			line = fileScanner.nextLine();
			if(line.length() == 0 || line.charAt(0)=='#') continue;
			String[] configStatement = line.split(" ");
			processConfigStatement(configStatement);
		}

		isConfigLoaded = true;
	}

	private static void processConfigStatement(String[] statement){
		switch (statement[0]){
			case "set":
				configVariablesMap.putIfAbsent(statement[1], statement[2]);
				break;
			case "keybind":
				keyBindingsMap.putIfAbsent(statement[1], statement[2]);
				break;
		}
	}

	public static String getConfigVariable(String name){
		if(configVariablesMap.containsKey(name)){
			return configVariablesMap.get(name);
		}
		return null;
	}

	public static ArrayList<Map.Entry<String, String>> getKeyBindings(){
		return new ArrayList<>(keyBindingsMap.entrySet());
	}
}
