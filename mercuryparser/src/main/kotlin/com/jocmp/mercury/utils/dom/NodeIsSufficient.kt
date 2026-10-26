package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Selection

// Given a node, determine if it's article-like enough to return
// param: node (a cheerio node)
// return: boolean
fun nodeIsSufficient(node: Selection): Boolean = node.text().trim().length >= 100
