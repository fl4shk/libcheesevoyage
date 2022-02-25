from setuptools import setup, find_packages

setup \
(
	name="libcheesevoyage",
	version="0.1",
	description="FL4SHK's personal Amaranth library",
	url="https://github.com/fl4shk/libcheesevoyage",
	author="Andrew Clark (FL4SHK)",
	author_email="fl4shk@users.noreply.github.com",
	license="MIT",
	packages=find_packages(exclude=["tests*"]),
	zip_safe=False,
)
