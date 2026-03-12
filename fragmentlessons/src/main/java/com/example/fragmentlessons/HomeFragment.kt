package com.example.fragmentlessons


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.fragmentlessons.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private val dataModel: DataModel by activityViewModels()
    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        // отправляем данные через value
//        binding.btnHome.setOnClickListener {
//            dataModel.sendToVideo("Данные из Home получены <-")
//            dataModel.sendToHome("Данные отправлены в Video ->")
//        }
//        // ждем полученные данные
//        dataModel.messageToHome.observe(viewLifecycleOwner,{ data ->
//            binding.textHome.text = data
//        })
    }


    companion object{
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}