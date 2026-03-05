package it.parkio.app.manager;

public class Park 
{
    private int m_id;
    private String m_name;

    Park(int id, String name)
    {
        m_id = id;
        m_name = name;
    }

    public int GetId()
    {
        return m_id;
    }

    public String GetName()
    {
        return m_name;
    }

    public String ToString()
    {
        return GetName();
    }

    public void SetId(int id)
    {
        m_id = id;
    }

    public void SetName(String name)
    {
        m_name = name;
    }

}
