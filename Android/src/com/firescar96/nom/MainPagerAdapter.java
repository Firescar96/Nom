package com.firescar96.nom;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;

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
	
	public MainFragment main;
	public OpenShareFragment open;
	public ClosedShareFragment1 closed1;
	public ClosedShareFragment2 closed2;
	
	public ArrayList<Integer> oldFragments = new ArrayList<Integer>();
	private ArrayList<Fragment> views = new ArrayList<Fragment>();
	
	private int pageCount = 1;
	
    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    	
    	Fragment newFragment = null;
    	
        switch (position)
        {
        case 0:
        	newFragment = new MainFragment();
        	main = (MainFragment) newFragment;
        	break;
        case 1:
        	if(context.findViewById(R.id.join_button) == null)
        	{
        		newFragment = new OpenShareFragment();
        		open = (OpenShareFragment) newFragment;
        	}
        	else if(context.findViewById(R.id.join_button).isSelected())
        	{
        		newFragment = new OpenShareFragment();
        		open = (OpenShareFragment) newFragment;
        	}
        	else if(context.findViewById(R.id.leave_button).isSelected())
        	{
        		newFragment = new ClosedShareFragment1();
        		closed1 = (ClosedShareFragment1) newFragment;
        	}
        	break;
        case 2:
        	newFragment = new ClosedShareFragment2();
        	closed2 = (ClosedShareFragment2) newFragment;
        	break;
        default:
        	newFragment = new MainFragment();
        	main = (MainFragment) newFragment;
        	break;
        }
        
        views.add(newFragment);
        return newFragment;
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
    	int index = views.indexOf (object);
    	for(int i = 0; i < oldFragments.size(); i++)
    		if(oldFragments.get(i) == index)
    		{
    			views.remove(index);
    			oldFragments.remove(i);
    			return POSITION_NONE;
    		}
    	
          return POSITION_UNCHANGED;
    }
}