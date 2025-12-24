package com.example.tvtvapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tvtvapp.R

class OptionFragment : Fragment() {
    companion object {
        private const val ARG_TITLE = "title"
        fun newInstance(title: String) = OptionFragment().apply {
            arguments = Bundle().apply { putString(ARG_TITLE, title) }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_option, container, false)
        val tv = root.findViewById<TextView>(R.id.optionText)
        tv.text = arguments?.getString(ARG_TITLE) ?: "Option"
        return root
    }
}
