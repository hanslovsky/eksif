package me.hanslovsky.eksif

import picocli.CommandLine

class RegexConverter : CommandLine.ITypeConverter<Regex> {
    override fun convert(value: String) = value.toRegex()
}