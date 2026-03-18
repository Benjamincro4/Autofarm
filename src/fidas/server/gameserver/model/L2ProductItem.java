package fidas.server.gameserver.model;

import java.util.ArrayList;

/**
 * Author: VISTALL
 * Company: ihgap
 * Date:  11:52:33/25.04.2010
 */
public class L2ProductItem
{
	private final int _productId;
	private final int _category;
	private final int _points;
	private ArrayList<L2ProductItemComponent> _components;

	public L2ProductItem(int productId, int category, int points)
	{
		_productId = productId;
		_category = category;
		_points = points;
	}

	public void setComponents(ArrayList<L2ProductItemComponent> a)
	{
		_components = a;
	}

	public ArrayList<L2ProductItemComponent> getComponents()
	{
		if(_components == null)
		{
			_components = new ArrayList<L2ProductItemComponent>();
		}

		return _components;
	}                      	

	public int getProductId()
	{
		return _productId;
	}

	public int getCategory()
	{
		return _category;
	}

	public int getPoints()
	{
		return _points;
	}                  	
}
