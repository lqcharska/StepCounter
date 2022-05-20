package com.example.step_counter.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.step_counter.R
import com.example.step_counter.databinding.GetStartedFragmentBinding

class GetStartedFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding: GetStartedFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.get_started_fragment,
            container,
            false)

        binding.getStartedButton.setOnClickListener {
            findNavController().navigate(GetStartedFragmentDirections.actionGetStartedToMain())
        }


        return binding.root
    }
}