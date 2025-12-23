API Reference
=============

This page lists all API calls implemented in the FeedHQ API. It is important
to read and understand the :doc:`terminology </api/terminology>` before refering
to this API reference.

user-info
---------

Returns various details about the user.

================= ===========================
URL               ``/reader/api/0/user-info``
Method            ``GET``
Supported formats XML, JSON
================= ===========================

unread-count
------------

Returns all streams that have unread items, along with their unread count and
the timestamp of their most recent item.

================= ==============================
URL               ``/reader/api/0/unread-count``
Method            ``GET``
Supported formats XML, JSON
================= ==============================

Sample JSON output:

.. code-block:: json

    {
        "max": 1000,
        "unreadcounts": [
            {
                "count": 1,
                "id": "feed/http://rss.slashdot.org/Slashdot/slashdot",
                "newestItemTimestampUsec": "1405452360000000"
            },
            {
                "count": 1,
                "id": "feed/http://feeds.feedburner.com/alistapart/main",
                "newestItemTimestampUsec": "1405432727000000"
            },
            {
                "count": 2,
                "id": "user/1/label/Tech",
                "newestItemTimestampUsec": "1405432727000000"
            },
            {
                "count": 2,
                "id": "user/1/state/com.google/reading-list",
                "newestItemTimestampUsec": "1405432727000000"
            }
        ]
    }

disable-tag
-----------

Deletes a category or a tag. Feeds that belong to the category being deleted
are moved to the top-level.

=================== ==============================
URL                 ``/reader/api/0/disable-tag``
Method              ``POST``
Supported formats   Returns "OK" in plain text
POST token required Yes
=================== ==============================

Required POST data:

* ``s`` the category's stream ID, **or** ``t``, the category (label) name.

rename-tag
----------

Renames a category.

=================== ==============================
URL                 ``/reader/api/0/rename-tag``
Method              ``POST``
Supported formats   Returns "OK" in plain text
POST token required Yes
=================== ==============================

Required POST data:

* ``s`` the category's stream ID, **or** ``t``, the category (label) name.
* ``dest``, the new label name, in its stream ID form: ``user/-/label/<new
  label>``.

subscription/list
-----------------

Lists all your subscriptions (feeds).

=================== ==============================
URL                 ``/reader/api/0/subscription/list``
Method              ``GET``
Supported formats   XML, JSON
=================== ==============================

Sample JSON output:

.. code-block:: json

    {
        "subscriptions": [
            {
                "title": "A List Apart",
                "firstitemmsec": "1373999174000",
                "htmlUrl": "http://alistapart.com",
                "sortid": "B0000000",
                "id": "feed/http://feeds.feedburner.com/alistapart/main",
                "categories": [
                    {
                        "id": "user/1/label/Tech",
                        "label": "Tech"
                    }
                ]
            }
        ]
    }

subscription/edit
-----------------

Creates, edits or deletes a subscription (feed).

=================== ==============================
URL                 ``/reader/api/0/subscription/edit``
Method              ``POST``
Supported formats   Returns "OK" in plain text
POST token required Yes
=================== ==============================

POST data for each action:

* Creation:

  * ``ac``: the string ``subscribe``
  * ``s``: the stream ID to create (``feed/<feed url>``).
  * ``t``: the name for this subscription.
  * (optional) ``a``: the stream ID of a category. If the category doesn't
    exist, it will be created.

* Edition:

  * ``ac``: the string ``edit``
  * ``s``: the stream ID to edit (``feed/<feed url>``).
  * ``r`` or ``a``: the stream ID of a category. ``r`` moves the feed out of
    the category, ``a`` adds the feed to the category.
  * ``t`` a new title for the feed.

* Deletion:

  * ``ac``: the string ``unsubscribe``
  * ``s``: the stream ID to delete (``feed/<feed url>``).

subscription/quickadd
---------------------

Adds a new subscription (feed), given only the feed's URL.

=================== ==============================
URL                 ``/reader/api/0/subscription/quickadd``
Method              ``POST``
Supported formats   XML, JSON
POST token required Yes
=================== ==============================

POST data:

* ``quickadd``: the URL of the feed, as a stream ID or just a standard URL.

Sample JSON output:

.. code-block:: json

    {
        "numResults": 1,
        "query": "http://feeds.feedburner.com/alistapart/main",
        "streamId": "feed/http://feeds.feedburner.com/alistapart/main",
    }

subscription/export
-------------------

Returns the list of subscriptions in OPML (XML) format.

=================== ==============================
URL                 ``/reader/api/0/subscription/export``
Method              ``GET``
Supported formats   XML (OPML)
=================== ==============================

subscription/import
-------------------

Imports all subscriptions from an OPML file.

=================== ==============================
URL                 ``/reader/api/0/subscription/import``
Method              ``POST``
Supported formats   Returns "OK: <count>" in plain text
=================== ==============================

Instead of form data, this API call expects the contents of the OPML file to
be provided directly in the request body.

subscribed
----------

Returns whether the user is subscribed to a given feed.

=================== ==============================
URL                 ``/reader/api/0/subscribed``
Method              ``GET``
Supported formats   Returns "true" or "false" in plain text
=================== ==============================

Querystring parameters:

* ``s``: the stream ID of the feed to check.

.. _streamcontents:

stream/contents
---------------

Returns paginated, detailed items for a given stream.

=================== ==============================
URL                 ``/reader/api/0/stream/contents/<stream ID>``
Method              ``GET``
Supported formats   XML, JSON, Atom
=================== ==============================

The stream ID is part of the URL. Additionally, the following querystring
parameters are supported:

* ``r``: sort criteria. Items are sorted by date (descending by default),
  ``r=o`` inverts the order.
* ``n``: the number of items per page. Default: 20.
* ``c``: the *continuation* string (see below).
* ``xt``: a stream ID to exclude from the list.
* ``it``: a steam ID to include in the list.
* ``ot``: an epoch timestamp. Items older than this timestamp are filtered
  out.
* ``nt``: an epoch timestamp. Items newer than this timestamp are filtered
  out.

*Continuation* is used for pagination. When FeedHQ returns a page, it contains
a ``continuation`` key that can be passed as a ``c`` parameter to fetch the
next page.

Sample JSON output:

.. code-block:: json

    {
        "direction": "ltr",
        "author": "brutasse",
        "title": "brutasse's reading list on FeedHQ",
        "updated": 1405538866,
        "continuation": "page2",
        "id": "user/1/state/com.google/reading-list"
        "self": [{
            "href": "https://feedhq.org/reader/api/0/stream/contents/user/-/state/com.google/reading-list?output=json"
        }],
        "items": []
    }

``items`` contains the list of feed items. Each item has the following
structure:

.. code-block:: json

    {
        "origin": {
        },
        "updated": 1405538866,
        "id": "tag:google.com,2005:reader/item/0000000009067698",
        "categories": [
            "user/1/state/com.google/reading-list",
            "user/1/label/Tech"
        ],
        "author": "Somebody",
        "alternate": [{
            "href": "http://example.com/href.html",
            "type": "text/html"
        }]
        "timestampUsec": "1405538280000000",
        "content": {
            "direction": "ltr",
            "content": "actual content",
        },
        "crawlTimeMsec": "1405538280000",
        "published": 1405538280,
        "title": "Example item test title"
    }

You'll notice that epoch timestamps are integers but when dates are expressed
in miliseconds (Msec) or microseconds (Usec) they are returned as strings.

stream/items/ids
----------------

Returns item IDs for a given stream ID.

=================== ==============================
URL                 ``/reader/api/0/stream/items/ids``
Method              ``GET``
Supported formats   XML, JSON
=================== ==============================

Querystring parameters:

* ``s``: the stream ID.
* ``n`` the number of item IDs per page to return.
* (optional) ``includeAllDirectStreamIds``: set it to ``true`` to include
  stream IDs in items.
* (optional) ``c``: the continuation string when requesting a page.
* (optional) ``xt``, ``it``, ``nt`` and ``ot`` are supported like in the
  :ref:`stream/contents <streamcontents>` API call.

stream/items/count
------------------

Returns the number of items in a given stream.

=================== ==============================
URL                 ``/reader/api/0/stream/items/count``
Method              ``GET``
Supported formats   Returns the count in plain text
=================== ==============================

Querystring parameters:

* ``s``: the stream ID.
* (optional) ``a``: set it to ``true`` to also get the date of the latest item
  in the stream.

Sample output, without ``a``::

    20174

Sample output, with ``a``::

    20174#July 16, 2014

stream/items/contents
---------------------

Returns the details about requested feed items.

=================== ==============================
URL                 ``/reader/api/0/stream/items/contents``
Method              ``GET``, ``POST``
Supported formats   XML, JSON, Atom
=================== ==============================

Items are requested via the ``i`` querystring parameter or post parameter. It
can be repeated as many times as needed. When requesting a large number of
items, it is recommended to use POST to avoid hitting URI length limits.

tag/list
--------

Returns the list of special tags and labels.

=================== ==============================
URL                 ``/reader/api/0/tag/list``
Method              ``GET``
Supported formats   XML, JSON
=================== ==============================

Sample JSON output:

.. code-block:: json

    {
        "tags": [
            {
                "id": "user/1/state/com.google/starred",
                "sortid": "A0000001"

            },
            {
                "id": "user/1/states/com.google/broadcast",
                "sortid": "A0000002"

            },
            {
                "id": "user/1/label/Tech",
                "sortid": "A0000003"
            },
        ]
    }

edit-tag
--------

Adds or remove tags from items. This API call is used to mark items as read or
unread or star / unstar items.

=================== ==============================
URL                 ``/reader/api/0/edit-tag``
Method              ``POST``
Supported formats   Returns "OK" in plain text
POST token required Yes
=================== ==============================

POST parameters:

* ``i``: ID of the item to edit. Can be repeated to edit multiple items at 
  once.
* ``a``: tag to add to the items. Can be repeated to add multiple tags at
  once.
* ``r``: tag to remove from the items. Can be repeated to remove multiple tags
  at once.

Possible tags are:

* ``user/-/state/com.google/kept-unread``
* ``user/-/state/com.google/starred``
* ``user/-/state/com.google/broadcast``
* ``user/-/state/com.google/read``

For example, to mark an item as read and star it at the same time::

    i=12345&a=user/-/state/com.google/starred&a=user/-/state/com.google/read

mark-all-as-read
----------------

Marks all items in a stream as read.

=================== ==============================
URL                 ``/reader/api/0/mark-all-as-read``
Method              ``POST``
Supported formats   Returns "OK" in plain text
POST token required Yes
=================== ==============================

POST parameters:

* ``s`` the stream ID to act on.
* (optional) ``ts``: an epoch timestamp **in microseconds**. When provided,
  only items *older* than this timestamp are marked as read.

preference/list
---------------

=================== ==============================
URL                 ``/reader/api/0/preference/list``
Method              ``GET``
Supported formats   XML, JSON
=================== ==============================

Returns a static response:

.. code-block:: json

    {
        "prefs": [{
            "id": "lhn-prefs",
            "value": "{\"subscriptions\":{\"ssa\":\"true\"}}"
        }]
    }

Yes, ``value`` is JSON-encoded JSON. ``ssa=true`` tells clients that
subscriptions are sorted alphabetically. FeedHQ doesn't support custom
sorting.

preference/stream/list
----------------------

=================== ==============================
URL                 ``/reader/api/0/preference/stream/list``
Method              ``GET``
Supported formats   XML, JSON
=================== ==============================

Returns a static response:

.. code-block:: json

    {
        "streamprefs": { }
    }

friend/list
-----------

=================== ==============================
URL                 ``/reader/api/0/friend/list``
Method              ``GET``
Supported formats   XML, JSON
=================== ==============================

Returns a single friend, the authenticated user:

.. code-block:: json

    {
        "friends": [{
            "p": "",
            "contactId": "-1",
            "flags": 1,
            "stream": "user/1/state/com.google/broadcast",
            "hasSharedItemsOnProfile": false,
            "profileIds": [
                "1"
            ],
            "userIds": [
                "1"
            ],
            "givenName": "brutasse",
            "displayName": "brutasse",
            "n": ""
        }]
    }

Undocumented / not implemented
------------------------------

The following API calls are known to exist in the Google Reader API but
haven't been implemented in the FeedHQ API:

* /related/list
* /stream/details
* /item/edit
* /item/delete
* /item/likers
* /friend/groups
* /friend/acl
* /friend/edit
* /friend/feeds
* /people/search
* /people/suggested
* /people/profile
* /comment/edit
* /conversation/edit
* /shorten-url
* /preference/set
* /preference/stream/set
* /search/items/ids
* /recommendation/edit
* /recommendation/list
* /list-user-bundle
* /edit-bundle
* /get-bundle
* /delete-bundle
* /bundles
* /list-friends-bunle
* /list-featured-bundle
