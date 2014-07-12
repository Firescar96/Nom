package com.firescar96.nom;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.firescar96.nom.ClosedShareFragment;
import com.firescar96.nom.MainActivity.MainFragment;
import com.firescar96.nom.MainActivity.OpenShareFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MainPagerAdapter extends FragmentStatePagerAdapter {

	MainActivity context = MainActivity.context;
	
	public Fragment curFrag;
	
	public ArrayList<Integer> oldFragments = new ArrayList<Integer>();
	private ArrayList<Fragment> views = new ArrayList<Fragment>();
	
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
        	break;
        case 1:
        	if(context.findViewById(R.id.open_button) == null)
        		newFragment = new OpenShareFragment();
        	else if(context.findViewById(R.id.open_button).isSelected())
        		newFragment = new OpenShareFragment();
        	else if(context.findViewById(R.id.closed_button).isSelected())
        		newFragment = new ClosedShareFragment();
        	break;
        default:
        	newFragment = new MainFragment();
        	break;
        }
        
        views.add(newFragment);
        curFrag = newFragment;
        return newFragment;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
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