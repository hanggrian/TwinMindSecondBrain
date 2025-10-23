package com.twinmind.wireframe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.twinmind.wireframe.databinding.FragmentChatsBinding

class ChatsFragment : Fragment() {
    lateinit var list1: ListView
    lateinit var list2: ListView
    lateinit var list3: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentChatsBinding.inflate(inflater).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list1 = view.findViewById(R.id.list1)
        list2 = view.findViewById(R.id.list2)
        list3 = view.findViewById(R.id.list3)

        list1.adapter =
            requireContext().createAdapter(
                "Email about last meeting" to
                    "Reply to this email based on our last meeting notes",
                "Marketing campaign" to
                    "Can you estimate the numbers requested in this call for our marketing " +
                    "campaign?",
            )
        list2.adapter =
            requireContext().createAdapter(
                "Shopping headphones" to
                    "Compare these tabs and find the best one to buy",
                "Study guide" to
                    "Create a study guide based on the tabs, attached pdfs, and cite web sources\n",
                "Finding apartments" to
                    "Find the best apartment close to restaurants with beautiful view",
            )
        list3.adapter =
            requireContext().createAdapter(
                "Plan draft" to
                    "Draft a detailed marketing plan based on the points we covered in the call.",
            )
    }
}
