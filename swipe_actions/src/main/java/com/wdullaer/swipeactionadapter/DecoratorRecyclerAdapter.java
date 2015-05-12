/*
 * Copyright 2014 Wouter Dullaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wdullaer.swipeactionadapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Helper class that provides functionality for adapters that need to wrap other adapters
 * Created by wdullaer on 04.06.14.
 */
public class DecoratorRecyclerAdapter extends RecyclerView.Adapter {
    protected final RecyclerView.Adapter mBaseAdapter;

    public DecoratorRecyclerAdapter(RecyclerView.Adapter baseAdapter){
        mBaseAdapter = baseAdapter;
    }

    @SuppressWarnings("unused")
    public RecyclerView.Adapter getAdapter(){
        return mBaseAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return mBaseAdapter.onCreateViewHolder(viewGroup, i);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return mBaseAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mBaseAdapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return mBaseAdapter.getItemId(position);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mBaseAdapter.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mBaseAdapter.registerAdapterDataObserver(observer);
    }
}
