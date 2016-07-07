package fr.unistra.ibmc.assemble2.io.computations;

import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

abstract public class Computation {

    protected Mediator mediator;

    protected Computation(Mediator mediator) {
        this.mediator = mediator;
    }

    protected String postData(String webservice, Map<String,String> data) throws Exception {
        StringBuffer allData = new StringBuffer();
        String answer = null;
        for (String key:data.keySet()) {
            if (allData.length() != 0)
                allData.append("&");
            allData.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(data.get(key), "UTF-8"));
        }
        try {
            URL url = null;
            if (AssembleConfig.getWebservicesAddress().get(0).endsWith("/api/"))
                url = new URL(AssembleConfig.getWebservicesAddress().get(0)+webservice);
            else if (AssembleConfig.getWebservicesAddress().get(0).endsWith("/api"))
                url = new URL(AssembleConfig.getWebservicesAddress().get(0)+"/"+webservice);
            else
                url = new URL(AssembleConfig.getWebservicesAddress().get(0)+"/api/"+webservice);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(allData.toString());
            wr.flush();

            StringBuffer result = new StringBuffer();
            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null)
                result.append(line+"\n");
            wr.close();
            rd.close();
            answer = result.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return answer;
    }

}
