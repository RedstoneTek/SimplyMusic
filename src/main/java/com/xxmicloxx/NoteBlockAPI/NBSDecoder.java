package com.xxmicloxx.NoteBlockAPI;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class NBSDecoder {
	
    public static Song parse(File decodeFile) {
        try {
            return parse(new FileInputStream(decodeFile), decodeFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Song parse(InputStream inputStream) {
        return parse(inputStream, null); // Source is unknown -> no file
    }

    private static Song parse(InputStream inputStream, File decodeFile) {
        HashMap<Integer, Layer> layerHashMap = new HashMap<Integer, Layer>();
        byte biggestInstrumentIndex = -1;
        
        try {
            DataInputStream dis = new DataInputStream(inputStream);
            short length = readShort(dis);
            short songHeight = readShort(dis);
            String title = readString(dis);
            String author = readString(dis);
            readString(dis);
            String description = readString(dis);
            float speed = readShort(dis) / 100f;
            dis.readBoolean(); // auto-save
            dis.readByte(); // auto-save duration
            dis.readByte(); // x/4ths, time signature
            readInt(dis); // minutes spent on project
            int leftClicks = readInt(dis); // left clicks (why?)
            readInt(dis); // right clicks (why?)
            readInt(dis); // blocks added
            readInt(dis); // blocks removed
            readString(dis); // .mid/.schematic file name
            short tick = -1;
            while (true) {
                short jumpTicks = readShort(dis); // jumps till next tick
                //System.out.println("Jumps to next tick: " + jumpTicks);
                if (jumpTicks == 0) {
                    break;
                }
                tick += jumpTicks;
                //System.out.println("Tick: " + tick);
                short layer = -1;
                while (true) {
                    short jumpLayers = readShort(dis); // jumps till next layer
                    if (jumpLayers == 0) {
                        break;
                    }
                    layer += jumpLayers;
                    //System.out.println("Layer: " + layer);
                    byte instrument = dis.readByte();
                    if (instrument > biggestInstrumentIndex){
                    	biggestInstrumentIndex = instrument;
                    }
                    setNote(layer, tick, instrument /* instrument */, dis.readByte() /* note */, layerHashMap);
                }
            }
            for (int i = 0; i < songHeight; i++) {
                Layer l = layerHashMap.get(i);
                
            	String name = readString(dis);
            	byte volume = dis.readByte();
                if (l != null) {
                    l.setName(name);
                    l.setVolume(volume);
                }
            }
            //count of custom instruments
            byte custom = dis.readByte();
            CustomInstrument[] customInstruments = new CustomInstrument[custom];

            for (int i = 0; i < custom; i++) {
                customInstruments[i] = new CustomInstrument((byte)i, readString(dis), readString(dis), dis.readByte(), dis.readByte());
            }
            
            if (Instrument.isCustomInstrument((byte) (biggestInstrumentIndex - custom))){
            	ArrayList<CustomInstrument> ci = CompatibilityUtils.get1_12Instruments();
            	ci.addAll(Arrays.asList(customInstruments));
            	customInstruments = ci.toArray(customInstruments);
            }
            
            return new Song(speed, layerHashMap, songHeight, length, title, author, description, decodeFile, customInstruments, leftClicks);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e){
        	String file = "";
        	if (decodeFile != null){
        		file = decodeFile.getName();
        	}
        	Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Song is corrupted " + file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e) { e.printStackTrace(); }
        
        return null;
    }

    private static void setNote(int layer, int ticks, byte instrument, byte key, HashMap<Integer, Layer> layerHashMap) {
        Layer l = layerHashMap.get(layer);
        if (l == null) {
            l = new Layer();
            layerHashMap.put(layer, l);
        }
        l.setNote(ticks, new Note(instrument, key));
    }

    private static short readShort(DataInputStream dis) throws IOException {
        int byte1 = dis.readUnsignedByte();
        int byte2 = dis.readUnsignedByte();
        return (short) (byte1 + (byte2 << 8));
    }

    private static int readInt(DataInputStream dis) throws IOException {
        int byte1 = dis.readUnsignedByte();
        int byte2 = dis.readUnsignedByte();
        int byte3 = dis.readUnsignedByte();
        int byte4 = dis.readUnsignedByte();
        return (byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24));
    }

    private static String readString(DataInputStream dis) throws IOException {
        int length = readInt(dis);
        if(length >= 1000) return "";
        if(length == 0) return "";
        StringBuilder sb = new StringBuilder(Math.min(length, Integer.MAX_VALUE));
        for (; length > 0; --length) {
            char c = (char) dis.readByte();
            if (c == (char) 0x0D) {
                c = ' ';
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
