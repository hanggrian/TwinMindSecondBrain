package com.twinmind.brain

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.toSpanned
import androidx.fragment.app.Fragment
import com.twinmind.brain.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    lateinit var noteTitleText1: TextView
    lateinit var noteTitleText2: TextView
    lateinit var noteContentText1: TextView
    lateinit var noteContentText2: TextView
    lateinit var noteContentText3: TextView

    lateinit var list1: ListView

    lateinit var todoText1: TextView
    lateinit var todoText2: TextView
    lateinit var todoText3: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentSearchBinding.inflate(inflater).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        noteTitleText1 = view.findViewById(R.id.noteTitleText1)
        noteTitleText2 = view.findViewById(R.id.noteTitleText2)
        noteContentText1 = view.findViewById(R.id.noteContentText1)
        noteContentText2 = view.findViewById(R.id.noteContentText2)
        noteContentText3 = view.findViewById(R.id.noteContentText3)
        list1 = view.findViewById(R.id.list1)
        todoText1 = view.findViewById(R.id.todoText1)
        todoText2 = view.findViewById(R.id.todoText2)
        todoText3 = view.findViewById(R.id.todoText3)

        val spans =
            arrayOf<Any>(
                ForegroundColorSpan(requireContext().getColor(R.color.orange)),
                StyleSpan(Typeface.BOLD),
            )

        noteTitleText1.text =
            buildSpannedString {
                append("Store opening ")
                inSpans(*spans) {
                    append("plan")
                }
            }
        noteContentText1.text = "Get budget approval, review market reesearch and choose locations."

        noteTitleText2.text =
            buildSpannedString {
                inSpans(*spans) {
                    append("Plan")
                }
                append(" for preso")
            }
        noteContentText2.text =
            buildSpannedString {
                append("First, we should talk about our ")
                inSpans(*spans) {
                    append("plan")
                }
                append(" for 2019 and how that maps to our data. But we want…")
            }
        noteContentText3.text =
            buildSpannedString {
                append("Chat with Erin about launch ")
                inSpans(*spans) {
                    append("plan")
                }
                append(".")
            }

        list1.adapter =
            SpannableArrayAdapter(
                requireContext(),
                buildSpannedString {
                    append("CS101 study ")
                    inSpans(*spans) {
                        append("plan")
                    }
                } to
                    "List all control flow statements (conditions, loops)".toSpanned(),
                "Product feature milestone".toSpanned() to
                    buildSpannedString {
                        append("Devise a ")
                        inSpans(*spans) {
                            append("plan")
                        }
                        append(" the launch of a new 'Advanced Analytics Dashboard' feature")
                    },
            )

        todoText1.text =
            buildSpannedString {
                inSpans(*spans) {
                    append("Plan")
                }
                append(" the agenda for the Q3 strategy meeting")
            }
        todoText2.text =
            buildSpannedString {
                append(
                    "Before finalizing the annual review, remember to\n" +
                        "check and sign-off on the strategic succession ",
                )
                inSpans(*spans) {
                    append("plan")
                }
                append("\ndocuments for all roles")
            }
        todoText3.text =
            buildSpannedString {
                append("Finalize the marketing ")
                inSpans(*spans) {
                    append("plan")
                }
                append(" presentation slides")
            }
    }

    class SpannableArrayAdapter(context: Context, vararg items: Item) :
        ArrayAdapter<Item>(context, android.R.layout.simple_list_item_2, items) {
        private val inflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view =
                convertView
                    ?: inflater.inflate(
                        android.R.layout.simple_list_item_2,
                        parent,
                        false,
                    )
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)
            getItem(position)!!.run {
                text1.text = first
                text2.text = second
            }
            return view
        }
    }
}
