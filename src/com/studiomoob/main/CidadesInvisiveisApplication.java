package com.studiomoob.main;

import com.parse.Parse;


import android.app.Application;

public class CidadesInvisiveisApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		Parse.initialize(this, "6mZAaZLhvnQ59yNNGxihpuU8Wo5fvtVqxirdPXMv", "dpRlgnuaYGduQxBMTxTNpzzXA3ErwUb7pnSy3sQa");
	}

}
