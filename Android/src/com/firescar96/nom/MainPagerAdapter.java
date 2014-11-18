package com.firescar96.nom;

import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import com.firescar96.nom.fragment.ClosedShareFragment1;
import com.firescar96.nom.fragment.ClosedShareFragment2;
import com.firescar96.nom.fragment.MainFragment;
import com.firescar96.nom.fragment.OpenShareFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {

	MainActivity context = MainActivity.context;
	
	private MainFragment main;
	private OpenShareFragment open;
	private ClosedShareFragment1 closed1;
	private ClosedShareFragment2 closed2;
	
	private final SparseArray<Fragment> views = new SparseArray<Fragment>();
	
	private int pageCount = 1;
	
    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
        case 0:
        	main = new MainFragment();
            views.put(position,main);
        	break;
        case 1:
        	if(!main.privacy)
        	{
        		open = new OpenShareFragment();
                views.put(position,open);
        	}
        	else
        	{
        		closed1 = new ClosedShareFragment1();
                views.put(position,closed1);
        	}
        	break;
        case 2:
        	closed2 = new ClosedShareFragment2();
            views.put(position,closed2);
        	break;
        default:
        	main = new MainFragment();
            views.put(position,main);
        	break;
        }
        
        return views.get(position);
    }

    public void setCount(int p) {
        // Show 3 total pages.
        pageCount = p;
    }
    
    @Override
    public int getCount() {
        // Show 3 total pages.
        return pageCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return context.getString(R.string.title_section2).toUpperCase(l);
            case 2:
                return context.getString(R.string.title_section3).toUpperCase(l);
        }
        return null;
    }
    
    @Override
    public int getItemPosition(Object object)
    {
    	for(int i=0; i < views.size(); i++)
    		if(views.indexOfValue((Fragment) object) != -1)
    	          return POSITION_UNCHANGED;
    	return POSITION_NONE;
    }

	public MainFragment getMain() {
		System.out.println("main val "+main);
		if(main == null)
			updateView(0);
		return main;
	}

	public OpenShareFragment getOpen() {
		if(open == null)
			updateView(1);
		return open;
	}
	
	public ClosedShareFragment1 getClosed1() {
		if(closed1 == null)
			updateView(1);
		return closed1;
	}

	public ClosedShareFragment2 getClosed2() {
		if(closed2 == null)
			updateView(2);
		return closed2;
	}
	
	public void updateView(int pos)
	{
		views.put(pos, null);
		notifyDataSetChanged();
	}
    
}