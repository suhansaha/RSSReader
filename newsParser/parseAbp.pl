#!/usr/bin/env perl
use WWW::Mechanize;

my $m = WWW::Mechanize->new();

my $url = 'http://www.anandabazar.com';
$m->get($url);


#open(my $fh, '>', 'dump.txt') or die "Could not open file '$filename' $!";

#print $fh $m->content();
$page = $m->content();
#close $fh;

my @menuLinks = $page =~ m/<a.*href="(.*?)".*department.*>(.*)<\/a>/g ;

my @coverStories = $page =~ m/<div.*?cover-story_container(.*?<div.*?cover_story_detail.*?>.*?)<\/div>/sg;

my $filename = 'rss.xml';
open(my $fh, '>', $filename) or die "Could not open file '$filename' $!";

print $fh "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<rss version=\"2.0\">\n<channel>\n";
print $fh "<title>AnandaBazar Patrika</title>\n";
print $fh "<link>$url</link>\n";
print $fh "<description>Leading Bengali Newspaper</description>\n";

foreach $story (@coverStories){
    $story =~ s/(&rsquo;)//g;
    $story =~ s/(&lsquo;)//g;
    $story =~ s/(&ldquo;)//g;
    $story =~ s/(&rdquo;)//g;
	#if( $story =~ m/<img.*?src="(.*?)".*?cover_story_detail.*?<a.*?href="(.*?)">(.*?)<\/a>.*?<a.*?>(.*?)<\/a>/sg )
	if( $story =~ m/<img.*?src=(.*?)>.*?cover_story_detail.*?<a.*?href="(.*?)">(.*?)<\/a>.*?<a.*?>(.*?)<\/a>/sg )
	{
		my $title = $3;
		my $link = $2;
		my $summary = $4;
		my $image = $1;

		$image =~ m/\'(.*?)\'/g;
		print $fh "<item>\n<title>$title</title>\n<link>$url$link</link>\n";
		print $fh "<description>&lt;img src=\"$url$1\"/&gt;$summary</description>\n</item>\n";
	}elsif($story =~ m/cover_story_detail.*?<a.*?href="(.*?)">(.*?)<\/a>.*?<a.*?>(.*?)<\/a>/sg){
		my $title = $2;
		my $link = $1;
		my $summary = $3;

		$image =~ m/\'(.*?)\'/g;
		print $fh "<item>\n<title>$title</title>\n<link>$url$link</link>\n";
		print $fh "<description>$summary</description>\n</item>\n";
	}

}

print $fh "\n</channel>\n</rss>";
close $fh;