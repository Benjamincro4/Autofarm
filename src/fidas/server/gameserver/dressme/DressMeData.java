package fidas.server.gameserver.dressme;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DressMeData
{
    private static final Logger _log = Logger.getLogger(DressMeData.class.getName());
    private static final DressMeData _instance = new DressMeData();

    private final List<DressMeArmorSet> _armorSets = new ArrayList<DressMeArmorSet>();
    private boolean _loaded = false;

    public static DressMeData getInstance()
    {
        return _instance;
    }

    public void ensureLoaded()
    {
        if (!_loaded)
        {
            load();
        }
    }

    public void load()
    {
        _armorSets.clear();

        try
        {
            File file = new File("data/xml/dressme/dressme.xml");
            if (!file.exists())
            {
                _log.warning("No existe: " + file.getPath());
                _loaded = true;
                return;
            }

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            NodeList list = doc.getElementsByTagName("armorSet");

            for (int i = 0; i < list.getLength(); i++)
            {
                Element e = (Element) list.item(i);

                int id = Integer.parseInt(e.getAttribute("id"));
                String name = e.getAttribute("name");

                int chest = parseInt(e.getAttribute("chest"));
                int legs = parseInt(e.getAttribute("legs"));
                int gloves = parseInt(e.getAttribute("gloves"));
                int boots = parseInt(e.getAttribute("boots"));
                int head = parseInt(e.getAttribute("head"));

                boolean premium = "true".equalsIgnoreCase(e.getAttribute("premium"));

                _armorSets.add(new DressMeArmorSet(id, name, chest, legs, gloves, boots, head, premium));
            }
        }
        catch (Exception ex)
        {
            _log.log(Level.WARNING, "DressMe: error cargando dressme.xml", ex);
        }
        finally
        {
            _loaded = true;
        }
    }

    private int parseInt(String s)
    {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    public List<DressMeArmorSet> getArmorSets()
    {
        return Collections.unmodifiableList(_armorSets);
    }

    public DressMeArmorSet getSet(int setId)
    {
        for (DressMeArmorSet s : _armorSets)
        {
            if (s.getId() == setId)
                return s;
        }
        return null;
    }
}
