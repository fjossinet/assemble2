package fr.unistra.ibmc.assemble2.io.computations.rnart;

import fr.unistra.ibmc.assemble2.utils.TBMath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Reads entirely the NUCLIN.DC file and stores the data
 */
public class NuclinDc {
    public ArrayList<String[]> names = new ArrayList<String[]>(); //step 1 -> String[]
    public ArrayList<NuclinDcDistance> distances = new ArrayList<NuclinDcDistance>(); //step 2 -> NuclinDcDistance
    public int iq = 0; //step 3
    public ArrayList<NuclinDcChiral> chirals = new ArrayList<NuclinDcChiral>(); // step 4 -> NuclinDcChiral
    public ArrayList<Integer> unknown = new ArrayList<Integer>(); // step 5 -> Integer
    public ArrayList<int[]> planes = new ArrayList<int[]>(); // step 6 -> int[40]
    Nuclin nuclin;

    public NuclinDc(Nuclin nuclin) throws Exception {
        this.nuclin = nuclin;
        parseNuclinDc();
    }

    public void parseNuclinDc() throws Exception {
        names = new ArrayList<String[]>(); //step 1 -> String[]
        distances = new ArrayList<NuclinDcDistance>(); //step 2 -> NuclinDcDistance
        iq = 0; //step 3
        chirals = new ArrayList<NuclinDcChiral>(); // step 4 -> NuclinDcChiral
        unknown = new ArrayList<Integer>(); // step 5 -> Integer
        planes = new ArrayList<int[]>(); // step 6 -> int[40]
        int step = 1;
        BufferedReader in = new BufferedReader(new FileReader(nuclin.nuclindc));
        String line;
        StringTokenizer tok;
        ArrayList<String> current = new ArrayList<String>();
        boolean l1 = true;
        int cur[] = new int[40];
        while ((line = in.readLine()) != null) {
            //Printing.verbose(line);
            tok = new StringTokenizer(line);

            //check if we change step
            switch (step) {
                case 1: {
                    if (TBMath.isNumber(line.substring(0, 6).replaceAll(" ", ""))) {
                        step++;
                        ArrayList<String> tmp = new ArrayList<String>();
                        for (String s : current) {
                            if (s.charAt(0) == 'X') {
                                if (tmp.size() > 0) {
                                    String[] param = new String[tmp.size()];
                                    for (int j = 0; j < tmp.size(); j++) {
                                        param[j] = tmp.get(j);
                                    }
                                    tmp = new ArrayList<String>();
                                    names.add(param);
                                }
                            } else {
                                tmp.add(s);
                            }
                        }
                    }
                }
                break;
                case 2: {
                    if (line.length() < 5) {
                        step++;
                    }
                }
                break;
                case 3: {
                    step++;
                }
                break;
                case 4: {
                    if (line.length() < 5) {
                        step++;
                    }

                }
                break;
                case 5: {
                    if (line.length() > 5) {
                        step++;
                    }
                }
                break;
            }

            switch (step) {
                case 1: {
                    if (tok.countTokens() < 10) {
                        System.out.println("count < 10 : " + tok.countTokens());
                        while (tok.countTokens() > 0) {
                            System.out.println(tok.nextToken());
                        }
                    } else {
                        for (int i = 0; i < 10; i++) {
                            current.add(tok.nextToken());
                        }
                    }
                }
                break;
                case 2: {
                    //            3    1  116   26   27  O14    C15   2.367    2
                    //            2    2    1    7    1  C4'  - N1    9.999    6    0	   ! Sugar-pyrimidine distances.
                    //            2    2    2    8    1  O4'  - N1    2.401    2    4

                    int a = Integer.parseInt(tok.nextToken());
                    int b = Integer.parseInt(tok.nextToken());
                    int c = Integer.parseInt(tok.nextToken());
                    int d = Integer.parseInt(tok.nextToken());
                    int e = Integer.parseInt(tok.nextToken());
                    String f = tok.nextToken();
                    String g = tok.nextToken();
                    if (g.charAt(0) == '-') {
                        g = tok.nextToken();
                    }
                    double h = Double.parseDouble(tok.nextToken());
                    int i = Integer.parseInt(tok.nextToken());
                    int j = 0;
                    if (tok.countTokens() > 0) {
                        String t = tok.nextToken();
                        if (t.charAt(0) != '!') {
                            j = Integer.parseInt(t);
                        }
                    }
                    distances.add(new NuclinDcDistance(a, b, c, d, e, f, g, h, i, j));
                }
                break;
                case 3: {
                    iq = Integer.parseInt(tok.nextToken());
                }
                break;
                case 4: {
                    //              7  6 13  8  2.49
                    int a = Integer.parseInt(tok.nextToken());
                    int b = Integer.parseInt(tok.nextToken());
                    int c = Integer.parseInt(tok.nextToken());
                    int d = Integer.parseInt(tok.nextToken());
                    double e = Double.parseDouble(tok.nextToken());
                    chirals.add(new NuclinDcChiral(a, b, c, d, e));
                }
                break;
                case 5: {
                    unknown.add(Integer.parseInt(tok.nextToken()));
                }
                break;
                case 6: {
                    int offset = 20;
                    if (l1) {
                        offset = 0;
                        cur = new int[40];
                    }
                    for (int i = offset; i < 20 + offset; i++) {
                        if (tok.countTokens() > 0) {
                            String t = tok.nextToken();
                            if (t.charAt(0) != '!') {
                                cur[i] = Integer.parseInt(t);
                            } else {
                                cur[i] = 0;
                            }
                        } else {
                            cur[i] = 0;
                        }
                    }
                    if (!l1) {
                        planes.add(cur);

                    }
                    l1 = !l1;
                }
                break;
            }
        }
        in.close();
        for (Integer t : unknown) {
            chirals.add(new NuclinDcChiral(t, 0, 0, 0, 0));
        }
    }
}

